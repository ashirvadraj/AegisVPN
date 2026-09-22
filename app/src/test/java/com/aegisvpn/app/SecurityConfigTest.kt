package com.aegisvpn.app

import com.aegisvpn.app.core.AegisOpenVpnParser
import com.aegisvpn.app.data.local.PreferencesManager
import com.wireguard.config.Config
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.KeyPair
import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress

class SecurityConfigTest {

    @Test
    fun testDefaultSecurityValues() {
        assertEquals("1.1.1.1", PreferencesManager.DNS_CLOUDFLARE)
        assertEquals("8.8.8.8", PreferencesManager.DNS_GOOGLE)
        assertEquals("9.9.9.9", PreferencesManager.DNS_QUAD9)
    }

    @Test
    fun testWireGuardConfigGeneration() {
        val keyPair = KeyPair()
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        assertNotNull(privateKey)
        assertNotNull(publicKey)

        val iface = Interface.Builder()
            .addAddress(InetNetwork.parse("172.16.0.2/32"))
            .addDnsServer(InetAddress.getByName("1.1.1.1"))
            .parsePrivateKey(privateKey.toBase64())
            .build()

        val peer = Peer.Builder()
            .parsePublicKey("bmXOC+F1FxEMF9dyiK2H5/1SUtzH0JuVo51h2wPfgyo=")
            .parseEndpoint("162.159.192.1:2408")
            .addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
            .build()

        val config = Config.Builder()
            .setInterface(iface)
            .addPeer(peer)
            .build()

        assertNotNull(config)
        assertEquals(1, config.peers.size)
    }

    @Test
    fun testAegisOpenVpnParser() {
        val sampleConfig = """
            client
            dev tun
            proto udp
            remote 219.100.37.161 1194
            cipher AES-128-CBC
            auth SHA1
            resolv-retry infinite
            nobind
            <ca>
            -----BEGIN CERTIFICATE-----
            MIIF...
            -----END CERTIFICATE-----
            </ca>
            <cert>
            -----BEGIN CERTIFICATE-----
            MIIC...
            -----END CERTIFICATE-----
            </cert>
            <key>
            -----BEGIN RSA PRIVATE KEY-----
            MIIE...
            -----END RSA PRIVATE KEY-----
            </key>
        """.trimIndent()

        val parsed = AegisOpenVpnParser.parse(sampleConfig, "Tokyo Test")
        assertNotNull(parsed)
        assertEquals("219.100.37.161", parsed.host)
        assertEquals(1194, parsed.port)
        assertEquals("udp", parsed.type)
        assertEquals("AES-128-CBC", parsed.cipher)
        assertEquals("SHA1", parsed.auth)
        assertTrue(parsed.ca?.contains("MIIF") == true)
        assertTrue(parsed.cert?.contains("MIIC") == true)
        assertTrue(parsed.key?.contains("MIIE") == true)
        assertNull(parsed.tlsCrypt)
    }
}
