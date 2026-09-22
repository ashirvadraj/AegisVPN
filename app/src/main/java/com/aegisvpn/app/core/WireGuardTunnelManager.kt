package com.aegisvpn.app.core

import android.content.Context
import com.aegisvpn.app.data.local.PreferencesManager
import com.aegisvpn.app.data.model.VpnServer
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Statistics
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class AegisTunnel(private val tunnelName: String = "aegis_wg") : Tunnel {
    override fun getName(): String = tunnelName

    override fun onStateChange(newState: Tunnel.State) {
        // State change callback from backend
    }
}

class WireGuardTunnelManager(private val context: Context) {

    private val backend: Backend = GoBackend(context.applicationContext)
    private val tunnel = AegisTunnel()
    private val prefs = PreferencesManager(context)

    suspend fun connect(server: VpnServer): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Get or create registered WireGuard keypair
            val (privateKey, clientIp) = WarpRegistrationManager.getOrCreateConfig(context)

            // 2. Build Interface (IP, DNS, Private Key)
            val dnsIp = prefs.selectedDnsProvider
            val ifaceBuilder = Interface.Builder()
                .addAddress(InetNetwork.parse("$clientIp/32"))
                .addDnsServer(InetAddress.getByName(dnsIp))
                .parsePrivateKey(privateKey)

            // Add secondary fallback DNS for zero leaks
            if (dnsIp == PreferencesManager.DNS_CLOUDFLARE) {
                ifaceBuilder.addDnsServer(InetAddress.getByName("1.0.0.1"))
            } else if (dnsIp == PreferencesManager.DNS_GOOGLE) {
                ifaceBuilder.addDnsServer(InetAddress.getByName("8.8.4.4"))
            }

            // 3. Build Peer (Server endpoint, AllowedIPs, Public Key)
            val peerBuilder = Peer.Builder()
                .parsePublicKey(WarpRegistrationManager.CLOUDFLARE_PEER_PUBLIC_KEY)
                .parseEndpoint("${server.ip}:${server.port}")
                .addAllowedIp(InetNetwork.parse("0.0.0.0/0"))

            if (prefs.isIpv6LeakProtectionEnabled) {
                try {
                    peerBuilder.addAllowedIp(InetNetwork.parse("::/0"))
                } catch (_: Exception) {}
            }

            val config = Config.Builder()
                .setInterface(ifaceBuilder.build())
                .addPeer(peerBuilder.build())
                .build()

            // 4. Activate Tunnel via GoBackend (Native WireGuard Engine)
            backend.setState(tunnel, Tunnel.State.UP, config)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun disconnect(): Boolean = withContext(Dispatchers.IO) {
        try {
            backend.setState(tunnel, Tunnel.State.DOWN, null)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getStatistics(): Statistics? {
        return try {
            backend.getStatistics(tunnel)
        } catch (_: Exception) {
            null
        }
    }
}
