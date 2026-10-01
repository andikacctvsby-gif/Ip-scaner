package com.example.network

import com.example.data.model.ScannedDevice
import com.example.data.vendor.VendorDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

class SubnetScanner {

    private var scanJob: Job? = null

    /**
     * Scans a subnet range asynchronously and reports progress and discovered devices in real time.
     */
    suspend fun startScan(
        baseIpPrefix: String, // e.g., "192.168.1"
        startHost: Int = 1,
        endHost: Int = 254,
        localIp: String?,
        gatewayIp: String?,
        deepScan: Boolean = false,
        onProgress: (scanned: Int, total: Int, currentIp: String) -> Unit,
        onDeviceFound: (ScannedDevice) -> Unit
    ) = coroutineScope {
        val total = (endHost - startHost + 1).coerceAtLeast(1)
        val scannedCount = AtomicInteger(0)
        // High-performance semaphore for network IO
        val concurrencyLimit = if (deepScan) 30 else 50
        val semaphore = Semaphore(concurrencyLimit)

        val probePorts = if (deepScan) {
            intArrayOf(80, 443, 22, 445, 554, 8080, 53, 9100, 5000, 3389, 8009, 62078)
        } else {
            intArrayOf(80, 443, 22, 445, 554, 8080, 53)
        }

        val deferredList = (startHost..endHost).map { host ->
            val targetIp = "$baseIpPrefix.$host"
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    if (!isActive) return@withPermit null

                    val isSelf = targetIp == localIp
                    val isGateway = targetIp == gatewayIp
                    var isAlive = isSelf
                    var latencyMs = if (isSelf) 1L else -1L
                    val openPorts = mutableListOf<Int>()

                    val startTime = System.currentTimeMillis()

                    if (!isSelf) {
                        // 1. Try socket probes on common ports
                        for (port in probePorts) {
                            if (!isActive) break
                            try {
                                Socket().use { socket ->
                                    socket.connect(InetSocketAddress(targetIp, port), 180)
                                    isAlive = true
                                    openPorts.add(port)
                                }
                            } catch (_: Exception) {}
                        }

                        // 2. If no ports connected, attempt ICMP / reachable ping
                        if (!isAlive && isActive) {
                            try {
                                val address = InetAddress.getByName(targetIp)
                                if (address.isReachable(200)) {
                                    isAlive = true
                                }
                            } catch (_: Exception) {}
                        }

                        if (isAlive) {
                            latencyMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                        }
                    }

                    // Progress reporting
                    val currentScanned = scannedCount.incrementAndGet()
                    onProgress(currentScanned, total, targetIp)

                    if (isAlive) {
                        // Retrieve MAC address from ARP cache
                        val mac = NetworkUtils.getMacFromArp(targetIp) ?: ""

                        // Resolve hostname (with brief timeout)
                        var hostname: String? = null
                        try {
                            val addr = InetAddress.getByName(targetIp)
                            val host = addr.canonicalHostName
                            if (!host.isNullOrBlank() && host != targetIp) {
                                hostname = host
                            }
                        } catch (_: Exception) {}

                        // Classify device category and match vendor
                        val (vendorName, category) = VendorDatabase.classifyDevice(
                            ipAddress = targetIp,
                            gatewayIp = gatewayIp,
                            macAddress = mac,
                            hostname = hostname,
                            openPorts = openPorts
                        )

                        val device = ScannedDevice(
                            ipAddress = targetIp,
                            macAddress = mac,
                            vendor = vendorName,
                            category = category,
                            hostname = hostname,
                            latencyMs = latencyMs,
                            openPorts = openPorts,
                            isCurrentDevice = isSelf,
                            isGateway = isGateway
                        )

                        onDeviceFound(device)
                        return@withPermit device
                    }

                    return@withPermit null
                }
            }
        }

        deferredList.awaitAll()
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
    }
}
