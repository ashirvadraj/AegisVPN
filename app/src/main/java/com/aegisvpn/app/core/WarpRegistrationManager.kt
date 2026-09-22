package com.aegisvpn.app.core

import android.content.Context
import com.aegisvpn.app.data.local.PreferencesManager
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WarpRegistrationManager {

    private const val WARP_REG_URL = "https://api.cloudflareclient.com/v0a2158/reg"
    const val CLOUDFLARE_PEER_PUBLIC_KEY = "bmXOC+F1FxEMF9dyiK2H5/1SUtzH0JuVo51h2wPfgyo="

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun getOrCreateConfig(context: Context): Pair<String, String> = withContext(Dispatchers.IO) {
        val prefs = PreferencesManager(context)
        val existingKey = prefs.warpPrivateKey
        val existingIp = prefs.warpAddressV4

        if (!existingKey.isNullOrBlank()) {
            return@withContext Pair(existingKey, existingIp)
        }

        // Generate new Curve25519 KeyPair
        val keyPair = KeyPair()
        val privateKeyBase64 = keyPair.privateKey.toBase64()
        val publicKeyBase64 = keyPair.publicKey.toBase64()

        try {
            val jsonPayload = JSONObject().apply {
                put("key", publicKeyBase64)
                put("install_id", "")
                put("fcm_token", "")
                put("tos", "2024-01-01T00:00:00.000Z")
                put("model", "Android")
                put("serial_number", "")
                put("locale", "en_US")
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(WARP_REG_URL)
                .post(requestBody)
                .header("User-Agent", "okhttp/3.12.1")
                .header("Content-Type", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val root = JSONObject(bodyStr)
                    val result = if (root.has("result") && !root.isNull("result")) root.getJSONObject("result") else root
                    val config = result.optJSONObject("config")
                    val iface = config?.optJSONObject("interface")
                    val addresses = iface?.optJSONObject("addresses")
                    val v4 = addresses?.optString("v4", "172.16.0.2") ?: "172.16.0.2"

                    prefs.warpPrivateKey = privateKeyBase64
                    prefs.warpAddressV4 = v4
                    return@withContext Pair(privateKeyBase64, v4)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback: save and use generated key with standard 172.16.0.2
        prefs.warpPrivateKey = privateKeyBase64
        prefs.warpAddressV4 = "172.16.0.2"
        Pair(privateKeyBase64, "172.16.0.2")
    }
}
