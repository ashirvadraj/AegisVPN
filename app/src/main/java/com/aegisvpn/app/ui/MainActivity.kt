package com.aegisvpn.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aegisvpn.app.R
import com.aegisvpn.app.core.VpnConnectionManager
import com.aegisvpn.app.core.VpnSessionState
import com.aegisvpn.app.core.VpnState
import com.aegisvpn.app.data.local.DefaultServers
import com.aegisvpn.app.data.local.PreferencesManager
import com.aegisvpn.app.data.model.VpnServer
import com.aegisvpn.app.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private var currentServer: VpnServer = DefaultServers.DEFAULT_FASTEST_SERVER

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            connectVpn()
        } else {
            Toast.makeText(this, "VPN permission required to establish tunnel", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Notification granted or denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)

        setupUI()
        setupListeners()
        observeVpnState()
        checkPermissions()
    }

    private fun setupUI() {
        // Initialize switch states
        binding.switchKillSwitch.isChecked = prefsManager.isKillSwitchEnabled
        binding.switchDnsProtect.isChecked = prefsManager.isDnsLeakProtectionEnabled
        binding.switchIpv6Protect.isChecked = prefsManager.isIpv6LeakProtectionEnabled

        updateServerCard(currentServer)
    }

    private fun setupListeners() {
        binding.powerButtonContainer.setOnClickListener {
            val currentState = VpnConnectionManager.sessionState.value.state
            if (currentState == VpnState.CONNECTED || currentState == VpnState.CONNECTING) {
                VpnConnectionManager.disconnect(this)
            } else {
                startVpnFlow()
            }
        }

        binding.cardServerSelector.setOnClickListener {
            val bottomSheet = ServerListBottomSheet { pickedServer ->
                currentServer = pickedServer
                VpnConnectionManager.setSelectedServer(pickedServer)
                updateServerCard(pickedServer)
                if (VpnConnectionManager.sessionState.value.state == VpnState.CONNECTED) {
                    Toast.makeText(this, "Reconnecting to ${pickedServer.countryLong}...", Toast.LENGTH_SHORT).show()
                    VpnConnectionManager.disconnect(this)
                    connectVpn()
                }
            }
            bottomSheet.show(supportFragmentManager, ServerListBottomSheet.TAG)
        }

        binding.btnAudit.setOnClickListener {
            val auditDialog = SecurityAuditDialog(this, this)
            auditDialog.show()
        }

        binding.switchKillSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isKillSwitchEnabled = isChecked
            Toast.makeText(
                this,
                if (isChecked) "Kill Switch Active: Leaks Blocked" else "Kill Switch Disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.switchDnsProtect.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isDnsLeakProtectionEnabled = isChecked
        }

        binding.switchIpv6Protect.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isIpv6LeakProtectionEnabled = isChecked
        }
    }

    private fun startVpnFlow() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            connectVpn()
        }
    }

    private fun connectVpn() {
        VpnConnectionManager.connect(this, currentServer)
    }

    private fun observeVpnState() {
        lifecycleScope.launch {
            VpnConnectionManager.sessionState.collectLatest { state ->
                renderState(state)
            }
        }
    }

    private fun renderState(session: VpnSessionState) {
        when (session.state) {
            VpnState.DISCONNECTED -> {
                binding.statusDot.setBackgroundColor(getColor(R.color.neon_red))
                binding.tvStatus.text = getString(R.string.status_disconnected)
                binding.tvStatus.setTextColor(getColor(R.color.neon_red))
                binding.ivPowerIcon.setColorFilter(getColor(R.color.text_secondary))
                binding.tvInstruction.text = getString(R.string.tap_to_connect)
                binding.cardLiveStats.visibility = View.GONE
                binding.powerButtonContainer.clearAnimation()

                if (session.errorMessage != null) {
                    Toast.makeText(this, session.errorMessage, Toast.LENGTH_LONG).show()
                }
            }
            VpnState.CONNECTING -> {
                binding.statusDot.setBackgroundColor(getColor(R.color.neon_amber))
                binding.tvStatus.text = getString(R.string.status_connecting)
                binding.tvStatus.setTextColor(getColor(R.color.neon_amber))
                binding.ivPowerIcon.setColorFilter(getColor(R.color.neon_amber))
                binding.tvInstruction.text = "Establishing secure military-grade tunnel..."
                binding.cardLiveStats.visibility = View.GONE

                val pulse = AlphaAnimation(0.3f, 1.0f).apply {
                    duration = 600
                    repeatMode = Animation.REVERSE
                    repeatCount = Animation.INFINITE
                }
                binding.powerButtonContainer.startAnimation(pulse)
            }
            VpnState.CONNECTED -> {
                binding.powerButtonContainer.clearAnimation()
                binding.statusDot.setBackgroundColor(getColor(R.color.neon_green))
                binding.tvStatus.text = getString(R.string.status_connected)
                binding.tvStatus.setTextColor(getColor(R.color.neon_green))
                binding.ivPowerIcon.setColorFilter(getColor(R.color.neon_green))
                binding.tvInstruction.text = getString(R.string.tap_to_disconnect)

                binding.cardLiveStats.visibility = View.VISIBLE

                // Update speeds & timers
                binding.tvDownloadSpeed.text = formatByteSpeed(session.downloadBps)
                binding.tvUploadSpeed.text = formatByteSpeed(session.uploadBps)
                binding.tvDuration.text = formatDuration(session.durationSeconds)

                if (session.server != null && session.server != currentServer) {
                    currentServer = session.server
                    updateServerCard(currentServer)
                }
            }
            VpnState.DISCONNECTING -> {
                binding.statusDot.setBackgroundColor(getColor(R.color.neon_amber))
                binding.tvStatus.text = getString(R.string.status_disconnecting)
                binding.tvStatus.setTextColor(getColor(R.color.neon_amber))
                binding.tvInstruction.text = "Closing tunnel..."
            }
        }
    }

    private fun updateServerCard(server: VpnServer) {
        binding.tvFlag.text = server.getCountryFlag()
        binding.tvServerName.text = if (server.isPreset) {
            "${server.countryLong} (Auto Fast)"
        } else {
            "${server.countryLong} (${server.ip})"
        }
        binding.tvServerPing.text = "Ping: ${server.formattedPing()} • ${server.formattedSpeed()}"
    }

    private fun formatByteSpeed(bytesPerSec: Long): String {
        return if (bytesPerSec >= 1_000_000) {
            String.format(Locale.US, "%.1f MB/s", bytesPerSec / 1_000_000.0)
        } else {
            String.format(Locale.US, "%.1f KB/s", bytesPerSec / 1_000.0)
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
