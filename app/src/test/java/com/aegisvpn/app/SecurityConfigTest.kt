package com.aegisvpn.app

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
}
