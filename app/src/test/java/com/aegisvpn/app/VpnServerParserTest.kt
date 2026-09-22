package com.aegisvpn.app

import com.aegisvpn.app.data.local.DefaultServers
import com.aegisvpn.app.data.model.VpnServer
import com.aegisvpn.app.data.repository.VpnServerRepository
import org.junit.Assert.*
import org.junit.Test

class VpnServerParserTest {

    private val repository = VpnServerRepository()

    @Test
    fun testParseVpnGateCsv_ValidRow() {
        val sampleCsv = """
*vpn_servers
#HostName,IP,Score,Ping,Speed,CountryLong,CountryShort,NumVpnSessions,Uptime,TotalUsers,TotalTraffic,LogType,Operator,Message,OpenVPN_ConfigData_Base64
vpn987654321.opengw.net,219.100.37.240,4392810,14,142982400,Japan,JP,12,123456,100,50000000,2week,Operator,Message,ZHVtbXktY29uZmln
*
        """.trimIndent()

        val result = repository.parseVpnGateCsv(sampleCsv)

        assertEquals(1, result.size)
        val server = result[0]
        assertEquals("vpn987654321.opengw.net", server.hostName)
        assertEquals("219.100.37.240", server.ip)
        assertEquals(14, server.pingMs)
        assertEquals("Japan", server.countryLong)
        assertEquals("JP", server.countryShort)
        assertEquals("ZHVtbXktY29uZmln", server.openVpnConfigBase64)
    }

    @Test
    fun testCountryFlagGeneration() {
        val usServer = VpnServer(
            hostName = "us.test",
            ip = "1.2.3.4",
            score = 100,
            pingMs = 20,
            speedBps = 1000000,
            countryLong = "United States",
            countryShort = "US"
        )
        assertEquals("🇺🇸", usServer.getCountryFlag())

        val jpServer = usServer.copy(countryShort = "JP")
        assertEquals("🇯🇵", jpServer.getCountryFlag())

        val deServer = usServer.copy(countryShort = "DE")
        assertEquals("🇩🇪", deServer.getCountryFlag())
    }

    @Test
    fun testFallbackServersAvailable() {
        val fallbacks = DefaultServers.FALLBACK_SERVERS
        assertTrue("Fallback servers pool must have at least 5 worldwide nodes", fallbacks.size >= 5)

        val countries = fallbacks.map { it.countryShort }.distinct()
        assertTrue("Fallback pool must span multiple continents", countries.size >= 4)
    }

    @Test
    fun testFormattedSpeedAndPing() {
        val server = VpnServer(
            hostName = "test",
            ip = "1.1.1.1",
            score = 100,
            pingMs = 42,
            speedBps = 55_000_000L,
            countryLong = "United States",
            countryShort = "US"
        )
        assertEquals("55.0 Mbps", server.formattedSpeed())
        assertEquals("42ms", server.formattedPing())
    }
}
