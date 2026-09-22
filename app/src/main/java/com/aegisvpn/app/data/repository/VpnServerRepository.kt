package com.aegisvpn.app.data.repository

import com.aegisvpn.app.data.local.DefaultServers
import com.aegisvpn.app.data.model.VpnServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class VpnServerRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var cachedServers: List<VpnServer> = DefaultServers.FALLBACK_SERVERS

    companion object {
        private const val VPNGATE_API_URL = "https://www.vpngate.net/api/iphone/"
    }

    suspend fun getServers(forceRefresh: Boolean = false): List<VpnServer> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedServers.size > DefaultServers.FALLBACK_SERVERS.size) {
            return@withContext cachedServers
        }

        try {
            val request = Request.Builder()
                .url(VPNGATE_API_URL)
                .header("User-Agent", "AegisVPN-Android-Client/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val csvBody = response.body?.string()
                if (!csvBody.isNullOrBlank()) {
                    val parsed = parseVpnGateCsv(csvBody)
                    if (parsed.isNotEmpty()) {
                        // Merge parsed with unique fallback servers
                        val combined = (parsed + DefaultServers.FALLBACK_SERVERS)
                            .distinctBy { it.ip }
                            .sortedWith(compareBy({ it.pingMs }, { -it.speedBps }))
                        cachedServers = combined
                        return@withContext combined
                    }
                }
            }
        } catch (e: Exception) {
            // Graceful fallback to embedded servers on network timeout or captive portal
            e.printStackTrace()
        }

        cachedServers = DefaultServers.FALLBACK_SERVERS
        DefaultServers.FALLBACK_SERVERS
    }

    fun parseVpnGateCsv(csv: String): List<VpnServer> {
        val servers = mutableListOf<VpnServer>()
        val lines = csv.lineSequence()

        var isDataSection = false
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("*")) continue

            if (trimmed.startsWith("#HostName")) {
                isDataSection = true
                continue
            }

            if (!isDataSection) continue

            // VPNGate CSV format:
            // 0: HostName, 1: IP, 2: Score, 3: Ping, 4: Speed, 5: CountryLong, 6: CountryShort,
            // 7: NumVpnSessions, 8: Uptime, 9: TotalUsers, 10: TotalTraffic, 11: LogType,
            // 12: Operator, 13: Message, 14: OpenVPN_ConfigData_Base64
            val tokens = trimmed.split(",")
            if (tokens.size >= 15) {
                try {
                    val host = tokens[0].trim()
                    val ip = tokens[1].trim()
                    val score = tokens[2].trim().toLongOrNull() ?: 0L
                    val ping = tokens[3].trim().toIntOrNull() ?: 99
                    val speed = tokens[4].trim().toLongOrNull() ?: 10_000_000L
                    val countryLong = tokens[5].trim()
                    val countryShort = tokens[6].trim()
                    val sessions = tokens[7].trim().toIntOrNull() ?: 0
                    val configBase64 = tokens[14].trim()

                    if (ip.isNotBlank() && countryShort.length == 2 && countryLong.isNotBlank()) {
                        servers.add(
                            VpnServer(
                                hostName = host,
                                ip = ip,
                                score = score,
                                pingMs = if (ping <= 0) 45 else ping,
                                speedBps = speed,
                                countryLong = countryLong,
                                countryShort = countryShort,
                                sessions = sessions,
                                openVpnConfigBase64 = configBase64,
                                isPreset = false
                            )
                        )
                    }
                } catch (_: Exception) {
                    // Skip malformed rows safely
                }
            }
        }

        return servers
    }
}
