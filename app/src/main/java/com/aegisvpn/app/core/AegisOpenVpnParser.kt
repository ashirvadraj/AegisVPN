package com.aegisvpn.app.core

import com.tim.openvpn.configuration.OpenVPNConfig
import java.util.regex.Pattern

object AegisOpenVpnParser {

    private val REMOTE_PATTERN = Pattern.compile("^remote\\s+(\\S+)\\s+(\\d+)", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
    private val PROTO_PATTERN = Pattern.compile("^proto\\s+(\\S+)", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
    private val CIPHER_PATTERN = Pattern.compile("^cipher\\s+(\\S+)", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
    private val AUTH_PATTERN = Pattern.compile("^auth\\s+(\\S+)", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
    private val CA_PATTERN = Pattern.compile("<ca>(.*?)</ca>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
    private val CERT_PATTERN = Pattern.compile("<cert>(.*?)</cert>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
    private val KEY_PATTERN = Pattern.compile("<key>(.*?)</key>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
    private val TLS_CRYPT_PATTERN = Pattern.compile("<tls-crypt>(.*?)</tls-crypt>", Pattern.DOTALL or Pattern.CASE_INSENSITIVE)

    fun parse(ovpnContent: String, serverName: String = "Japan Exit Relay"): OpenVPNConfig {
        var host = "219.100.37.161"
        var port = 1194
        val remoteMatcher = REMOTE_PATTERN.matcher(ovpnContent)
        if (remoteMatcher.find()) {
            host = remoteMatcher.group(1) ?: host
            port = remoteMatcher.group(2)?.toIntOrNull() ?: port
        }

        var proto = "udp"
        val protoMatcher = PROTO_PATTERN.matcher(ovpnContent)
        if (protoMatcher.find()) {
            val p = protoMatcher.group(1)?.lowercase() ?: "udp"
            proto = if (p.contains("tcp")) "tcp" else "udp"
        }

        var cipher = "AES-128-CBC"
        val cipherMatcher = CIPHER_PATTERN.matcher(ovpnContent)
        if (cipherMatcher.find()) {
            cipher = cipherMatcher.group(1) ?: cipher
        }

        var auth = "SHA1"
        val authMatcher = AUTH_PATTERN.matcher(ovpnContent)
        if (authMatcher.find()) {
            auth = authMatcher.group(1) ?: auth
        }

        var ca = ""
        val caMatcher = CA_PATTERN.matcher(ovpnContent)
        if (caMatcher.find()) {
            ca = caMatcher.group(1)?.trim() ?: ""
        }

        var cert = ""
        val certMatcher = CERT_PATTERN.matcher(ovpnContent)
        if (certMatcher.find()) {
            cert = certMatcher.group(1)?.trim() ?: ""
        }

        var key = ""
        val keyMatcher = KEY_PATTERN.matcher(ovpnContent)
        if (keyMatcher.find()) {
            key = keyMatcher.group(1)?.trim() ?: ""
        }

        var tlsCrypt: String? = null
        val tlsCryptMatcher = TLS_CRYPT_PATTERN.matcher(ovpnContent)
        if (tlsCryptMatcher.find()) {
            tlsCrypt = tlsCryptMatcher.group(1)?.trim()
        }

        return OpenVPNConfig(
            name = serverName,
            host = host,
            port = port,
            type = proto,
            cipher = cipher,
            auth = auth,
            ca = ca,
            key = key,
            cert = cert,
            tlsCrypt = tlsCrypt,
            configuration = ovpnContent
        )
    }
}
