package com.aegisvpn.app.data.model

data class SecurityAuditResult(
    val isVpnTunnelActive: Boolean,
    val dnsLeakSecure: Boolean,
    val ipv6Shielded: Boolean,
    val isEncryptedTraffic: Boolean,
    val activeResolver: String,
    val testTimestamp: Long = System.currentTimeMillis()
) {
    val isSafe: Boolean
        get() = isVpnTunnelActive && dnsLeakSecure && ipv6Shielded
}
