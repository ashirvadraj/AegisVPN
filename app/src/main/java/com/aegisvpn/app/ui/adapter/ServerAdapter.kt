package com.aegisvpn.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aegisvpn.app.R
import com.aegisvpn.app.data.model.VpnServer
import com.aegisvpn.app.databinding.ItemVpnServerBinding

class ServerAdapter(
    private val onServerSelected: (VpnServer) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    private val items = mutableListOf<VpnServer>()

    fun submitList(newServers: List<VpnServer>) {
        items.clear()
        items.addAll(newServers)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val binding = ItemVpnServerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ServerViewHolder(
        private val binding: ItemVpnServerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(server: VpnServer) {
            binding.tvItemFlag.text = server.getCountryFlag()
            binding.tvItemCountry.text = server.countryLong
            binding.tvItemDetails.text = if (server.isPreset) {
                "${server.ip} • Dedicated Global Node"
            } else {
                "${server.ip} • Public Relay (${server.sessions} active)"
            }
            binding.tvItemPing.text = server.formattedPing()
            binding.tvItemSpeed.text = server.formattedSpeed()

            val pingColor = if (server.pingMs < 60) {
                R.color.neon_green
            } else if (server.pingMs < 120) {
                R.color.neon_amber
            } else {
                R.color.neon_red
            }
            binding.tvItemPing.setTextColor(
                ContextCompat.getColor(binding.root.context, pingColor)
            )

            binding.root.setOnClickListener {
                onServerSelected(server)
            }
        }
    }
}
