package com.aegisvpn.app.data.local

import com.aegisvpn.app.data.model.VpnServer

object DefaultServers {

    val FALLBACK_SERVERS = listOf(
        VpnServer(
            hostName = "jp-tokyo.opengw.net",
            ip = "219.100.37.161",
            score = 1000000,
            pingMs = 11,
            speedBps = 1_641_000_000L,
            countryLong = "Japan (Tokyo University - Unblock)",
            countryShort = "JP",
            sessions = 180,
            port = 443,
            protocol = "openvpn",
            isPreset = true
        ),
        VpnServer(
            hostName = "fastest.anycast.cloudflareclient.com",
            ip = "162.159.192.1",
            score = 999999,
            pingMs = 18,
            speedBps = 150_000_000L,
            countryLong = "Auto Fastest (Global Edge)",
            countryShort = "US",
            sessions = 120,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "us-east.cloudflareclient.com",
            ip = "162.159.193.1",
            score = 985000,
            pingMs = 24,
            speedBps = 120_000_000L,
            countryLong = "United States",
            countryShort = "US",
            sessions = 95,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "de-frankfurt.cloudflareclient.com",
            ip = "162.159.195.1",
            score = 940000,
            pingMs = 29,
            speedBps = 110_000_000L,
            countryLong = "Germany",
            countryShort = "DE",
            sessions = 84,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "uk-london.cloudflareclient.com",
            ip = "162.159.192.2",
            score = 930000,
            pingMs = 31,
            speedBps = 105_000_000L,
            countryLong = "United Kingdom",
            countryShort = "GB",
            sessions = 78,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "sg-central.cloudflareclient.com",
            ip = "162.159.192.4",
            score = 910000,
            pingMs = 38,
            speedBps = 95_000_000L,
            countryLong = "Singapore",
            countryShort = "SG",
            sessions = 62,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "ca-toronto.cloudflareclient.com",
            ip = "162.159.192.5",
            score = 920000,
            pingMs = 33,
            speedBps = 100_000_000L,
            countryLong = "Canada",
            countryShort = "CA",
            sessions = 58,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "nl-amsterdam.cloudflareclient.com",
            ip = "162.159.192.6",
            score = 935000,
            pingMs = 28,
            speedBps = 110_000_000L,
            countryLong = "Netherlands",
            countryShort = "NL",
            sessions = 71,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "fr-paris.cloudflareclient.com",
            ip = "162.159.192.7",
            score = 915000,
            pingMs = 32,
            speedBps = 98_000_000L,
            countryLong = "France",
            countryShort = "FR",
            sessions = 65,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "au-sydney.cloudflareclient.com",
            ip = "162.159.192.8",
            score = 890000,
            pingMs = 45,
            speedBps = 85_000_000L,
            countryLong = "Australia",
            countryShort = "AU",
            sessions = 45,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        ),
        VpnServer(
            hostName = "in-mumbai.cloudflareclient.com",
            ip = "162.159.192.9",
            score = 945000,
            pingMs = 22,
            speedBps = 115_000_000L,
            countryLong = "India",
            countryShort = "IN",
            sessions = 90,
            port = 2408,
            protocol = "wireguard",
            isPreset = true
        )
    )

    val DEFAULT_FASTEST_SERVER: VpnServer
        get() = FALLBACK_SERVERS.first()
}
