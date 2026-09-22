package com.aegisvpn.app.core

import com.aegisvpn.app.data.model.VpnServer

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

data class VpnSessionState(
    val state: VpnState = VpnState.DISCONNECTED,
    val server: VpnServer? = null,
    val durationSeconds: Long = 0,
    val downloadBps: Long = 0,
    val uploadBps: Long = 0,
    val totalBytesIn: Long = 0,
    val totalBytesOut: Long = 0,
    val errorMessage: String? = null
)
