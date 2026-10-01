package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ScanHistoryEntity
import com.example.data.local.ScannedDeviceEntity
import com.example.data.model.ScannedDevice
import com.example.network.NetworkUtils
import com.example.network.SubnetScanner
import com.example.network.WifiNetworkDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScannerRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val deviceDao = database.deviceDao()
    private val historyDao = database.scanHistoryDao()
    private val scanner = SubnetScanner()

    fun getSavedDevices(): Flow<List<ScannedDevice>> {
        return deviceDao.getAllDevices().map { entities ->
            val details = NetworkUtils.getNetworkDetails(context)
            entities.map { entity ->
                entity.toScannedDevice(
                    isCurrentDevice = entity.ipAddress == details.ipAddress,
                    isGateway = entity.ipAddress == details.gatewayIp
                )
            }
        }
    }

    fun getFavoriteDevices(): Flow<List<ScannedDevice>> {
        return deviceDao.getFavoriteDevices().map { entities ->
            val details = NetworkUtils.getNetworkDetails(context)
            entities.map { entity ->
                entity.toScannedDevice(
                    isCurrentDevice = entity.ipAddress == details.ipAddress,
                    isGateway = entity.ipAddress == details.gatewayIp
                )
            }
        }
    }

    fun getScanHistory(): Flow<List<ScanHistoryEntity>> {
        return historyDao.getHistory()
    }

    suspend fun getDeviceByIp(ip: String): ScannedDeviceEntity? = withContext(Dispatchers.IO) {
        deviceDao.getDeviceByIp(ip)
    }

    suspend fun updateAliasAndNotes(ip: String, alias: String?, notes: String?) = withContext(Dispatchers.IO) {
        deviceDao.updateAliasAndNotes(ip, alias, notes)
    }

    suspend fun toggleFavorite(ip: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        deviceDao.updateFavorite(ip, isFavorite)
    }

    suspend fun deleteDevice(ip: String) = withContext(Dispatchers.IO) {
        val existing = deviceDao.getDeviceByIp(ip)
        if (existing != null) {
            deviceDao.deleteDevice(existing)
        }
    }

    suspend fun saveDiscoveredDevice(device: ScannedDevice) = withContext(Dispatchers.IO) {
        val existing = deviceDao.getDeviceByIp(device.ipAddress)
        val entity = if (existing != null) {
            existing.copy(
                macAddress = if (device.macAddress.isNotBlank()) device.macAddress else existing.macAddress,
                vendor = if (device.vendor != "Tidak diketahui") device.vendor else existing.vendor,
                categoryName = device.category.name,
                hostname = device.hostname ?: existing.hostname,
                lastSeenTimestamp = System.currentTimeMillis(),
                lastLatencyMs = device.latencyMs,
                openPortsString = if (device.openPorts.isNotEmpty()) device.openPorts.joinToString(",") else existing.openPortsString
            )
        } else {
            ScannedDeviceEntity.fromScannedDevice(device)
        }
        deviceDao.upsertDevice(entity)
    }

    suspend fun recordScanHistory(
        networkSsid: String,
        gatewayIp: String,
        subnetRange: String,
        devicesCount: Int,
        durationSeconds: Long
    ) = withContext(Dispatchers.IO) {
        historyDao.insertHistory(
            ScanHistoryEntity(
                timestamp = System.currentTimeMillis(),
                networkSsid = networkSsid,
                gatewayIp = gatewayIp,
                subnetRange = subnetRange,
                devicesFoundCount = devicesCount,
                durationSeconds = durationSeconds
            )
        )
    }

    fun getNetworkInfo(): WifiNetworkDetails {
        return NetworkUtils.getNetworkDetails(context)
    }

    suspend fun getPublicIp(): String? {
        return NetworkUtils.fetchPublicIp()
    }

    fun stopScan() {
        scanner.stopScan()
    }

    suspend fun executeScan(
        baseIpPrefix: String,
        startHost: Int,
        endHost: Int,
        localIp: String?,
        gatewayIp: String?,
        deepScan: Boolean,
        onProgress: (scanned: Int, total: Int, currentIp: String) -> Unit,
        onDeviceFound: (ScannedDevice) -> Unit
    ) {
        scanner.startScan(
            baseIpPrefix = baseIpPrefix,
            startHost = startHost,
            endHost = endHost,
            localIp = localIp,
            gatewayIp = gatewayIp,
            deepScan = deepScan,
            onProgress = onProgress,
            onDeviceFound = { device ->
                onDeviceFound(device)
            }
        )
    }

    fun exportToFormattedText(devices: List<ScannedDevice>, networkInfo: WifiNetworkDetails): String {
        val sb = StringBuilder()
        sb.appendLine("=== HASIL PEMINDAIAN IP SCANNER ===")
        sb.appendLine("WiFi SSID: ${networkInfo.ssid}")
        sb.appendLine("Gateway: ${networkInfo.gatewayIp}")
        sb.appendLine("Subnet Mask: ${networkInfo.subnetMask}")
        sb.appendLine("Total Perangkat: ${devices.size}")
        sb.appendLine("Waktu: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        sb.appendLine("----------------------------------------")
        devices.forEachIndexed { index, dev ->
            sb.appendLine("${index + 1}. [${dev.category.title}] ${dev.displayName}")
            sb.appendLine("   IP: ${dev.ipAddress}")
            if (dev.macAddress.isNotBlank()) sb.appendLine("   MAC: ${dev.macAddress}")
            sb.appendLine("   Vendor: ${dev.vendor}")
            if (!dev.hostname.isNullOrBlank()) sb.appendLine("   Hostname: ${dev.hostname}")
            if (dev.latencyMs >= 0) sb.appendLine("   Ping: ${dev.latencyMs} ms")
            if (dev.openPorts.isNotEmpty()) sb.appendLine("   Port Terbuka: ${dev.openPorts.joinToString(", ")}")
            if (!dev.notes.isNullOrBlank()) sb.appendLine("   Catatan: ${dev.notes}")
            sb.appendLine()
        }
        return sb.toString()
    }
}
