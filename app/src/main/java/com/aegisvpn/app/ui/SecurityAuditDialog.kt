package com.aegisvpn.app.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.aegisvpn.app.R
import com.aegisvpn.app.databinding.DialogSecurityAuditBinding
import com.aegisvpn.app.security.SecurityAuditor
import kotlinx.coroutines.launch

class SecurityAuditDialog(
    context: Context,
    private val lifecycleOwner: LifecycleOwner
) : Dialog(context) {

    private lateinit var binding: DialogSecurityAuditBinding
    private val auditor = SecurityAuditor(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogSecurityAuditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.btnAuditClose.setOnClickListener {
            dismiss()
        }

        runAuditTest()
    }

    private fun runAuditTest() {
        lifecycleOwner.lifecycleScope.launch {
            val result = auditor.runAudit()

            if (result.isVpnTunnelActive) {
                binding.tvAuditTunnelIcon.text = "🛡️"
                binding.tvAuditTunnelDesc.text = "Active • AES-256 encrypted tunnel (tun0 interface active)"
                binding.tvAuditTunnelDesc.setTextColor(context.getColor(R.color.neon_green))
            } else {
                binding.tvAuditTunnelIcon.text = "⚠️"
                binding.tvAuditTunnelDesc.text = "Inactive • Tap power button to encrypt traffic"
                binding.tvAuditTunnelDesc.setTextColor(context.getColor(R.color.neon_amber))
            }

            if (result.dnsLeakSecure) {
                binding.tvAuditDnsIcon.text = "🔒"
                binding.tvAuditDnsDesc.text = "Protected • Resolving via ${result.activeResolver} (No ISP Leaks)"
                binding.tvAuditDnsDesc.setTextColor(context.getColor(R.color.neon_green))
            } else {
                binding.tvAuditDnsIcon.text = "⚠️"
                binding.tvAuditDnsDesc.text = "Vulnerable • Potential DNS query exposure to ISP"
                binding.tvAuditDnsDesc.setTextColor(context.getColor(R.color.neon_red))
            }

            if (result.ipv6Shielded) {
                binding.tvAuditIpv6Icon.text = "🛡️"
                binding.tvAuditIpv6Desc.text = "Shielded • Cellular & Wi-Fi IPv6 bypass disabled"
                binding.tvAuditIpv6Desc.setTextColor(context.getColor(R.color.neon_green))
            } else {
                binding.tvAuditIpv6Icon.text = "⚠️"
                binding.tvAuditIpv6Desc.text = "Exposed • IPv6 interface active outside tunnel"
                binding.tvAuditIpv6Desc.setTextColor(context.getColor(R.color.neon_amber))
            }
        }
    }
}
