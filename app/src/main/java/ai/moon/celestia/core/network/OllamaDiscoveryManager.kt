package ai.moon.celestia.core.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiscoveredServer(
    val serviceName: String,
    val hostAddress: String,
    val port: Int
) {
    val httpUrl: String get() = "http://$hostAddress:$port"
}

class OllamaDiscoveryManager(context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _discoveredServers = MutableStateFlow<List<DiscoveredServer>>(emptyList())
    val discoveredServers: StateFlow<List<DiscoveredServer>> = _discoveredServers.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun startDiscovery() {
        if (_isSearching.value || nsdManager == null) return

        _discoveredServers.value = emptyList()
        _isSearching.value = true

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: $errorCode")
                stopDiscovery()
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: $errorCode")
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.d(TAG, "Discovery started for $serviceType")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.d(TAG, "Discovery stopped")
                _isSearching.value = false
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo == null) return
                // Try resolving anything that looks like it could be our target server, e.g. Ollama or generic local nodes
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
                
                try {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Log.e(TAG, "Resolve failed for ${serviceInfo?.serviceName}: $errorCode")
                        }

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                            if (resolvedInfo == null) return
                            val host = resolvedInfo.host?.hostAddress ?: return
                            val port = resolvedInfo.port
                            val name = resolvedInfo.serviceName

                            // Only add if it relates to ollama or if the port is 11434 (default ollama port)
                            if (name.contains("ollama", ignoreCase = true) || port == 11434) {
                                val server = DiscoveredServer(
                                    serviceName = name,
                                    hostAddress = host,
                                    port = port
                                )

                                val current = _discoveredServers.value.toMutableList()
                                if (current.none { it.hostAddress == server.hostAddress && it.port == server.port }) {
                                    current.add(server)
                                    _discoveredServers.value = current
                                }
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during resolveService", e)
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo == null) return
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                val current = _discoveredServers.value.filterNot { it.serviceName == serviceInfo.serviceName }
                _discoveredServers.value = current
            }
        }

        try {
            // Some environments use HTTP while others might just use _ollama._tcp.
            // Using a standard HTTP web service discovery increases the chance to catch local nodes too.
            nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            // It might fail to bind if another app is discovering. The fallback will happen in error blocks.
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start mDNS service discovery", e)
            _isSearching.value = false
        }
    }

    fun stopDiscovery() {
        if (!_isSearching.value || nsdManager == null) return
        try {
            discoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping discovery", e)
        } finally {
            discoveryListener = null
            _isSearching.value = false
        }
    }

    companion object {
        private const val TAG = "OllamaDiscovery"
    }
}
