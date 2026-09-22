package com.aegisvpn.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aegisvpn.app.data.local.DefaultServers
import com.aegisvpn.app.data.model.VpnServer
import com.aegisvpn.app.data.repository.VpnServerRepository
import com.aegisvpn.app.databinding.BottomSheetServersBinding
import com.aegisvpn.app.ui.adapter.ServerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class ServerListBottomSheet(
    private val onServerPicked: (VpnServer) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetServersBinding? = null
    private val binding get() = _binding!!

    private val repository = VpnServerRepository()
    private lateinit var adapter: ServerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetServersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ServerAdapter { server ->
            onServerPicked(server)
            dismiss()
        }

        binding.rvServers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServers.adapter = adapter

        binding.itemAutoFastest.setOnClickListener {
            onServerPicked(DefaultServers.DEFAULT_FASTEST_SERVER)
            dismiss()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadServers(forceRefresh = true)
        }

        loadServers(forceRefresh = false)
    }

    private fun loadServers(forceRefresh: Boolean) {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val servers = repository.getServers(forceRefresh)
            binding.swipeRefresh.isRefreshing = false
            adapter.submitList(servers)
            val countries = servers.map { it.countryShort }.distinct().size
            binding.tvServerCount.text = "${servers.size} free worldwide public relays available ($countries countries)"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ServerListBottomSheet"
    }
}
