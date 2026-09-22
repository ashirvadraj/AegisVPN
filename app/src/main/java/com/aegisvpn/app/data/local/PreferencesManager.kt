package com.aegisvpn.app.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "aegis_vpn_security_prefs"
        private const val KEY_KILL_SWITCH = "kill_switch_enabled"
        private const val KEY_DNS_LEAK_PROTECT = "dns_leak_protect_enabled"
        private const val KEY_IPV6_LEAK_PROTECT = "ipv6_leak_protect_enabled"
        private const val KEY_DNS_PROVIDER = "selected_dns_provider"
        private const val KEY_LAST_SERVER_IP = "last_selected_server_ip"
        private const val KEY_WARP_PRIVATE_KEY = "warp_private_key"
        private const val KEY_WARP_ADDRESS_V4 = "warp_address_v4"
        private const val KEY_WARP_ADDRESS_V6 = "warp_address_v6"

        const val DNS_CLOUDFLARE = "1.1.1.1"
        const val DNS_GOOGLE = "8.8.8.8"
        const val DNS_QUAD9 = "9.9.9.9"
    }

    var isKillSwitchEnabled: Boolean
        get() = prefs.getBoolean(KEY_KILL_SWITCH, true)
        set(value) = prefs.edit().putBoolean(KEY_KILL_SWITCH, value).apply()

    var isDnsLeakProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_DNS_LEAK_PROTECT, true)
        set(value) = prefs.edit().putBoolean(KEY_DNS_LEAK_PROTECT, value).apply()

    var isIpv6LeakProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_IPV6_LEAK_PROTECT, true)
        set(value) = prefs.edit().putBoolean(KEY_IPV6_LEAK_PROTECT, value).apply()

    var selectedDnsProvider: String
        get() = prefs.getString(KEY_DNS_PROVIDER, DNS_CLOUDFLARE) ?: DNS_CLOUDFLARE
        set(value) = prefs.edit().putString(KEY_DNS_PROVIDER, value).apply()

    var lastSelectedServerIp: String?
        get() = prefs.getString(KEY_LAST_SERVER_IP, null)
        set(value) = prefs.edit().putString(KEY_LAST_SERVER_IP, value).apply()

    var warpPrivateKey: String?
        get() = prefs.getString(KEY_WARP_PRIVATE_KEY, null)
        set(value) = prefs.edit().putString(KEY_WARP_PRIVATE_KEY, value).apply()

    var warpAddressV4: String
        get() = prefs.getString(KEY_WARP_ADDRESS_V4, "172.16.0.2") ?: "172.16.0.2"
        set(value) = prefs.edit().putString(KEY_WARP_ADDRESS_V4, value).apply()

    var warpAddressV6: String?
        get() = prefs.getString(KEY_WARP_ADDRESS_V6, null)
        set(value) = prefs.edit().putString(KEY_WARP_ADDRESS_V6, value).apply()
}
