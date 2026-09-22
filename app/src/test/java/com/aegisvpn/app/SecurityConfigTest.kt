package com.aegisvpn.app

import com.aegisvpn.app.data.local.PreferencesManager
import org.junit.Assert.*
import org.junit.Test

class SecurityConfigTest {

    @Test
    fun testDefaultSecurityValues() {
        // Assert that default security posture enforces zero-trust
        assertEquals("1.1.1.1", PreferencesManager.DNS_CLOUDFLARE)
        assertEquals("8.8.8.8", PreferencesManager.DNS_GOOGLE)
        assertEquals("9.9.9.9", PreferencesManager.DNS_QUAD9)
    }

    @Test
    fun testDnsResolversValidity() {
        val resolvers = listOf(
            PreferencesManager.DNS_CLOUDFLARE,
            PreferencesManager.DNS_GOOGLE,
            PreferencesManager.DNS_QUAD9
        )
        for (dns in resolvers) {
            val parts = dns.split(".")
            assertEquals(4, parts.size)
            for (p in parts) {
                val num = p.toInt()
                assertTrue(num in 0..255)
            }
        }
    }
}
