package com.aegisvpn.app.core

import android.content.Context
import android.content.Intent
import com.aegisvpn.app.data.local.DefaultServers
import com.aegisvpn.app.data.model.VpnServer
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

    private var lastBytesIn = 0L
    private var lastBytesOut = 0L

    fun connect(context: Context, server: VpnServer) {
        _sessionState.update {
            it.copy(
                state = VpnState.CONNECTING,
                server = server,
                errorMessage = null,
                durationSeconds = 0
            )
        }

        val intent = Intent(context, AegisVpnService::class.java).apply {
            action = AegisVpnService.ACTION_CONNECT
            putExtra(AegisVpnService.EXTRA_SERVER, server)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun disconnect(context: Context) {
        _sessionState.update {
            it.copy(state = VpnState.DISCONNECTING)
        }

        val intent = Intent(context, AegisVpnService::class.java).apply {
            action = AegisVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun setSelectedServer(server: VpnServer) {
        if (_sessionState.value.state == VpnState.DISCONNECTED) {
            _sessionState.update { it.copy(server = server) }
        }
    }

    internal fun onConnecting(server: VpnServer) {
        _sessionState.update {
            it.copy(state = VpnState.CONNECTING, server = server)
        }
    }

    internal fun onConnected(server: VpnServer) {
        lastBytesIn = AegisVpnService.totalBytesIn.get()
        lastBytesOut = AegisVpnService.totalBytesOut.get()

        _sessionState.update {
            it.copy(
                state = VpnState.CONNECTED,
                server = server,
                durationSeconds = 0,
                errorMessage = null
            )
        }

        startSessionTicker()
    }

    internal fun onDisconnected() {
        tickerJob?.cancel()
        _sessionState.update {
            it.copy(
                state = VpnState.DISCONNECTED,
                downloadBps = 0,
                uploadBps = 0
            )
        }
    }

    internal fun onError(message: String) {
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

    private fun startSessionTicker() {
        tickerJob?.cancel()
        tickerJob = managerScope.launch {
            while (isActive) {
                delay(1000)
                val curBytesIn = AegisVpnService.totalBytesIn.get()
                val curBytesOut = AegisVpnService.totalBytesOut.get()

                val dlSpeed = (curBytesIn - lastBytesIn).coerceAtLeast(0)
                val ulSpeed = (curBytesOut - lastBytesOut).coerceAtLeast(0)

                lastBytesIn = curBytesIn
                lastBytesOut = curBytesOut

                _sessionState.update {
                    if (it.state == VpnState.CONNECTED) {
                        it.copy(
                            durationSeconds = it.durationSeconds + 1,
                            downloadBps = dlSpeed,
                            uploadBps = ulSpeed,
                            totalBytesIn = curBytesIn,
                            totalBytesOut = curBytesOut
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }
}
