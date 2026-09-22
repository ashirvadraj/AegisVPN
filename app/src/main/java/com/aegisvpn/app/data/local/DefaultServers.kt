package com.aegisvpn.app.data.local

import com.aegisvpn.app.data.model.VpnServer

object DefaultServers {

    val FALLBACK_SERVERS = listOf(
        VpnServer(
            hostName = "us-east.aegisvpn.net",
            ip = "198.51.100.12",
            score = 985000,
            pingMs = 28,
            speedBps = 95_000_000L,
            countryLong = "United States",
            countryShort = "US",
            sessions = 42,
            isPreset = true
        ),
        VpnServer(
            hostName = "de-frankfurt.aegisvpn.net",
            ip = "198.51.100.24",
            score = 920000,
            pingMs = 35,
            speedBps = 88_000_000L,
            countryLong = "Germany",
            countryShort = "DE",
            sessions = 31,
            isPreset = true
        ),
        VpnServer(
            hostName = "uk-london.aegisvpn.net",
            ip = "198.51.100.36",
            score = 890000,
            pingMs = 32,
            speedBps = 82_000_000L,
            countryLong = "United Kingdom",
            countryShort = "GB",
            sessions = 29,
            isPreset = true
        ),
        VpnServer(
            hostName = "jp-tokyo.aegisvpn.net",
            ip = "198.51.100.48",
            score = 950000,
            pingMs = 45,
            speedBps = 90_000_000L,
            countryLong = "Japan",
            countryShort = "JP",
            sessions = 55,
            isPreset = true
        ),
        VpnServer(
            hostName = "sg-central.aegisvpn.net",
            ip = "198.51.100.60",
            score = 870000,
            pingMs = 52,
            speedBps = 75_000_000L,
            countryLong = "Singapore",
            countryShort = "SG",
            sessions = 38,
            isPreset = true
        ),
        VpnServer(
            hostName = "ca-toronto.aegisvpn.net",
            ip = "198.51.100.72",
            score = 840000,
            pingMs = 38,
            speedBps = 70_000_000L,
            countryLong = "Canada",
            countryShort = "CA",
            sessions = 22,
            isPreset = true
        ),
        VpnServer(
            hostName = "nl-amsterdam.aegisvpn.net",
            ip = "198.51.100.84",
            score = 860000,
            pingMs = 30,
            speedBps = 85_000_000L,
            countryLong = "Netherlands",
            countryShort = "NL",
            sessions = 26,
            isPreset = true
        ),
        VpnServer(
            hostName = "fr-paris.aegisvpn.net",
            ip = "198.51.100.96",
            score = 830000,
            pingMs = 34,
            speedBps = 78_000_000L,
            countryLong = "France",
            countryShort = "FR",
            sessions = 20,
            isPreset = true
        )
    )

    val DEFAULT_FASTEST_SERVER: VpnServer
        get() = FALLBACK_SERVERS.first()
}
