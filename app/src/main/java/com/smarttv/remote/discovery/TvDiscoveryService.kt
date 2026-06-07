package com.smarttv.remote.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.smarttv.remote.model.TvDevice
import com.smarttv.remote.util.FileLogger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

class TvDiscoveryService(private val context: Context) {

    companion object {
        private const val TAG = "TvDiscoveryService"
        private const val SERVICE_TYPE = "_androidtvremote._tcp."
    }

    private var nsdManager: NsdManager? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var isDiscovering = false

    private val _discoveredDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<TvDevice>> = _discoveredDevices.asStateFlow()

    private val resolvedDevices = mutableMapOf<String, TvDevice>()

    fun startDiscovery() {
        try {
            if (isDiscovering) {
                FileLogger.d(TAG, "Already discovering")
                return
            }

            nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
            if (nsdManager == null) {
                FileLogger.e(TAG, "NSD Manager not available")
                return
            }

            _discoveredDevices.value = emptyList()
            resolvedDevices.clear()

            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    isDiscovering = true
                    FileLogger.i(TAG, "Discovery started for $regType")
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    isDiscovering = false
                    FileLogger.i(TAG, "Discovery stopped")
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    isDiscovering = false
                    FileLogger.e(TAG, "Start discovery failed: errorCode=$errorCode")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    isDiscovering = false
                    FileLogger.e(TAG, "Stop discovery failed: errorCode=$errorCode")
                }

                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    try {
                        FileLogger.d(TAG, "Service found: ${serviceInfo.serviceName} (type=${serviceInfo.serviceType})")
                        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                FileLogger.w(TAG, "Resolve failed for ${serviceInfo.serviceName}: errorCode=$errorCode")
                            }

                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                try {
                                    val host = serviceInfo.host?.hostAddress ?: return
                                    val port = serviceInfo.port
                                    val name = serviceInfo.serviceName.replace(".$SERVICE_TYPE", "")
                                        .replace("\\.[^.]*$", "")

                                    FileLogger.i(TAG, "Resolved TV: $name @ $host:$port")

                                    val device = TvDevice(
                                        id = host,
                                        name = name,
                                        host = host,
                                        port = port,
                                        serviceName = serviceInfo.serviceName
                                    )

                                    resolvedDevices[host] = device
                                    _discoveredDevices.value = resolvedDevices.values.toList()
                                } catch (e: Exception) {
                                    FileLogger.e(TAG, "Error processing resolved service", e)
                                }
                            }
                        })
                    } catch (e: Exception) {
                        FileLogger.e(TAG, "Error in onServiceFound", e)
                    }
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                    try {
                        val host = resolvedDevices.entries
                            .find { it.value.serviceName == serviceInfo.serviceName }
                            ?.key
                        if (host != null) {
                            resolvedDevices.remove(host)
                            _discoveredDevices.value = resolvedDevices.values.toList()
                            FileLogger.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                        }
                    } catch (e: Exception) {
                        FileLogger.e(TAG, "Error in onServiceLost", e)
                    }
                }
            }

            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            isDiscovering = false
            FileLogger.e(TAG, "Failed to start discovery", e)
        }
    }

    fun stopDiscovery() {
        try {
            if (isDiscovering) {
                nsdManager?.stopServiceDiscovery(discoveryListener)
                isDiscovering = false
                FileLogger.d(TAG, "Discovery stopped")
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error stopping discovery", e)
        }
    }

    fun destroy() {
        try {
            stopDiscovery()
            nsdManager = null
            discoveryListener = null
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error destroying discovery service", e)
        }
    }
}
