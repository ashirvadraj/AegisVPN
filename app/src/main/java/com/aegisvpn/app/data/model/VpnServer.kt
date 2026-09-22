package com.aegisvpn.app.data.model

import java.io.Serializable
import java.util.Locale

data class VpnServer(
    val hostName: String,
    val ip: String,
    val score: Long,
    val pingMs: Int,
    val speedBps: Long,
    val countryLong: String,
    val countryShort: String,
    val sessions: Int = 0,
    val openVpnConfigBase64: String = "",
    val port: Int = 1194,
    val protocol: String = "udp",
    val isPreset: Boolean = false
) : Serializable {

    fun getCountryFlag(): String {
        if (countryShort.length != 2) return "🌐"
        val code = countryShort.uppercase(Locale.US)
        val firstChar = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    fun formattedSpeed(): String {
        val mbps = speedBps / 1_000_000.0
        return if (mbps >= 1.0) {
            String.format(Locale.US, "%.1f Mbps", mbps)
        } else {
            val kbps = speedBps / 1_000.0
            String.format(Locale.US, "%.0f Kbps", kbps)
        }
    }

    fun formattedPing(): String {
        return if (pingMs > 0) "${pingMs}ms" else "Fast"
    }

    fun getDisplayName(): String {
        return "${getCountryFlag()} $countryLong"
    }
}
