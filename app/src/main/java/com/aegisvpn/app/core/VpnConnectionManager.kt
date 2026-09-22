package com.aegisvpn.app.core

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aegisvpn.app.AegisApplication
import com.aegisvpn.app.R
import com.aegisvpn.app.data.local.DefaultServers
import com.aegisvpn.app.data.model.VpnServer
import com.aegisvpn.app.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object VpnConnectionManager {

    private val _sessionState = MutableStateFlow(
        VpnSessionState(
            state = VpnState.DISCONNECTED,
            server = DefaultServers.DEFAULT_FASTEST_SERVER
        )
    )
    val sessionState: StateFlow<VpnSessionState> = _sessionState.asStateFlow()

    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null
    private var tunnelManager: WireGuardTunnelManager? = null

    private var lastRx = 0L
    private var lastTx = 0L

    private fun getTunnelManager(context: Context): WireGuardTunnelManager {
        if (tunnelManager == null) {
            tunnelManager = WireGuardTunnelManager(context.applicationContext)
        }
        return tunnelManager!!
    }

    fun connect(context: Context, server: VpnServer) {
        _sessionState.update {
            it.copy(
                state = VpnState.CONNECTING,
                server = server,
                errorMessage = null,
                durationSeconds = 0
            )
        }

        managerScope.launch {
            val tm = getTunnelManager(context)
            val success = tm.connect(server)
            if (success) {
                onConnected(context, server)
            } else {
                onError("Failed to establish secure WireGuard tunnel. Check internet connection.")
            }
        }
    }

    fun disconnect(context: Context) {
        _sessionState.update {
            it.copy(state = VpnState.DISCONNECTING)
        }

        managerScope.launch {
            val tm = getTunnelManager(context)
            tm.disconnect()
            onDisconnected(context)
        }
    }

    fun setSelectedServer(server: VpnServer) {
        if (_sessionState.value.state == VpnState.DISCONNECTED) {
            _sessionState.update { it.copy(server = server) }
        }
    }

    private fun onConnected(context: Context, server: VpnServer) {
        lastRx = 0L
        lastTx = 0L

        _sessionState.update {
            it.copy(
                state = VpnState.CONNECTED,
                server = server,
                durationSeconds = 0,
                errorMessage = null
            )
        }

        showOngoingNotification(context, server)
        startSessionTicker(context)
    }

    private fun onDisconnected(context: Context) {
        tickerJob?.cancel()
        removeNotification(context)

        _sessionState.update {
            it.copy(
                state = VpnState.DISCONNECTED,
                downloadBps = 0,
                uploadBps = 0
            )
        }
    }

    private fun onError(message: String) {
        tickerJob?.cancel()
        _sessionState.update {
            it.copy(
                state = VpnState.DISCONNECTED,
                errorMessage = message,
                downloadBps = 0,
                uploadBps = 0
            )
        }
    }

    private fun startSessionTicker(context: Context) {
        tickerJob?.cancel()
        tickerJob = managerScope.launch {
            val tm = getTunnelManager(context)
            while (isActive) {
                delay(1000)
                val stats = tm.getStatistics()
                val curRx = stats?.totalRx() ?: 0L
                val curTx = stats?.totalTx() ?: 0L

                val dlSpeed = if (lastRx > 0 && curRx >= lastRx) (curRx - lastRx) else 0L
                val ulSpeed = if (lastTx > 0 && curTx >= lastTx) (curTx - lastTx) else 0L

                if (curRx > 0) lastRx = curRx
                if (curTx > 0) lastTx = curTx

                _sessionState.update {
                    if (it.state == VpnState.CONNECTED) {
                        it.copy(
                            durationSeconds = it.durationSeconds + 1,
                            downloadBps = dlSpeed,
                            uploadBps = ulSpeed,
                            totalBytesIn = curRx,
                            totalBytesOut = curTx
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }

    private fun showOngoingNotification(context: Context, server: VpnServer) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingMain = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, AegisApplication.VPN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_check)
            .setContentTitle("AegisVPN - " + server.countryLong)
            .setContentText("Connected & Encrypted (WireGuard • " + server.ip + ")")
            .setContentIntent(pendingMain)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        nm.notify(AegisApplication.NOTIFICATION_ID, notification)
    }

    private fun removeNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(AegisApplication.NOTIFICATION_ID)
    }
}
