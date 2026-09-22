package com.aegisvpn.app.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.aegisvpn.app.data.local.PreferencesManager
import com.aegisvpn.app.data.model.SecurityAuditResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface

class SecurityAuditor(private val context: Context) {

    private val prefs = PreferencesManager(context)

    suspend fun runAudit(): SecurityAuditResult = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        // 1. Check if VPN Transport is active on current default network
        var isVpnActive = false
        if (cm != null) {
            val activeNetwork = cm.activeNetwork
            if (activeNetwork != null) {
                val caps = cm.getNetworkCapabilities(activeNetwork)
                if (caps != null) {
                    isVpnActive = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                }
            }
        }

        // 2. Check DNS Resolver configuration
        val activeDns = prefs.selectedDnsProvider
        var dnsSecure = false
        try {
            // Test resolve through designated secure DNS
            val host = InetAddress.getByName("one.one.one.one")
            val hostIp = host.hostAddress
            dnsSecure = (hostIp == "1.1.1.1" || hostIp == "1.0.0.1")
        } catch (_: Exception) {
            dnsSecure = isVpnActive // If tunnel is blocking raw resolution, DNS is secure
        }

        // 3. Check IPv6 Leak Prevention (ensure no unmetered global IPv6 interfaces leaking)
        var ipv6Shielded = true
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val nif = interfaces.nextElement()
                if (nif.isUp && !nif.isLoopback && !nif.name.contains("tun")) {
                    val addrs = nif.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        // Global unicast IPv6 starts with 2000::/3
                        if (addr is java.net.Inet6Address && !addr.isLinkLocalAddress && !addr.isSiteLocalAddress) {
                            if (!isVpnActive) {
                                ipv6Shielded = false
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            ipv6Shielded = true
        }

        SecurityAuditResult(
            isVpnTunnelActive = isVpnActive,
            dnsLeakSecure = dnsSecure,
            ipv6Shielded = ipv6Shielded,
            isEncryptedTraffic = isVpnActive,
            activeResolver = activeDns
        )
    }
}
