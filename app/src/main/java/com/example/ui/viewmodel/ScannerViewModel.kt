package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ScanHistoryEntity
import com.example.data.model.DeviceCategory
import com.example.data.model.OpenPortInfo
import com.example.data.model.ScannedDevice
import com.example.data.repository.ScannerRepository
import com.example.network.NetworkUtils
import com.example.network.PingResult
import com.example.network.PingStats
import com.example.network.PingTester
import com.example.network.PortScanner
import com.example.network.WifiNetworkDetails
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val scannedCount: Int = 0,
    val totalCount: Int = 254,
    val currentIpScanned: String = "",
    val devices: List<ScannedDevice> = emptyList(),
    val networkDetails: WifiNetworkDetails = WifiNetworkDetails(),
    val searchQuery: String = "",
    val selectedCategoryFilter: DeviceCategory? = null,
    val deepScanEnabled: Boolean = false,
    val scanDurationSeconds: Long = 0,
    val selectedDevice: ScannedDevice? = null,
    val alertMessage: String? = null
)

data class PortScanState(
    val targetIp: String = "",
    val isScanning: Boolean = false,
    val openPorts: List<OpenPortInfo> = emptyList(),
    val currentPort: Int = 0,
    val progress: Float = 0f
)

data class PingState(
    val targetIp: String = "",
    val isRunning: Boolean = false,
    val results: List<PingResult> = emptyList(),
    val stats: PingStats = PingStats()
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScannerRepository(application)

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _portScanState = MutableStateFlow(PortScanState())
    val portScanState: StateFlow<PortScanState> = _portScanState.asStateFlow()

    private val _pingState = MutableStateFlow(PingState())
    val pingState: StateFlow<PingState> = _pingState.asStateFlow()

    val savedDevices: StateFlow<List<ScannedDevice>> = repository.getSavedDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scanHistory: StateFlow<List<ScanHistoryEntity>> = repository.getScanHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var scanJob: Job? = null
    private var portScanJob: Job? = null
    private var pingJob: Job? = null

    init {
        refreshNetworkInfo()
    }

    fun refreshNetworkInfo() {
        val details = repository.getNetworkInfo()
        _uiState.update { it.copy(networkDetails = details) }

        viewModelScope.launch {
            val publicIp = repository.getPublicIp()
            if (publicIp != null) {
                _uiState.update { state ->
                    state.copy(networkDetails = state.networkDetails.copy(publicIp = publicIp))
                }
            }
        }
    }

    fun toggleDeepScan(enabled: Boolean) {
        _uiState.update { it.copy(deepScanEnabled = enabled) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: DeviceCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun startSubnetScan(
        customPrefix: String? = null,
        startHost: Int = 1,
        endHost: Int = 254
    ) {
        if (_uiState.value.isScanning) return

        refreshNetworkInfo()
        val details = _uiState.value.networkDetails

        val basePrefix = if (!customPrefix.isNullOrBlank()) {
            customPrefix.trim()
        } else {
            val ipParts = details.ipAddress.split(".")
            if (ipParts.size == 4) "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}" else "192.168.1"
        }

        scanJob?.cancel()
        val foundDevices = mutableListOf<ScannedDevice>()

        _uiState.update {
            it.copy(
                isScanning = true,
                progress = 0f,
                scannedCount = 0,
                totalCount = (endHost - startHost + 1).coerceAtLeast(1),
                currentIpScanned = "$basePrefix.$startHost",
                devices = emptyList(),
                scanDurationSeconds = 0
            )
        }

        val scanStartTime = System.currentTimeMillis()

        scanJob = viewModelScope.launch {
            try {
                repository.executeScan(
                    baseIpPrefix = basePrefix,
                    startHost = startHost,
                    endHost = endHost,
                    localIp = details.ipAddress,
                    gatewayIp = details.gatewayIp,
                    deepScan = _uiState.value.deepScanEnabled,
                    onProgress = { scanned, total, currentIp ->
                        val progress = scanned.toFloat() / total.toFloat()
                        _uiState.update {
                            it.copy(
                                scannedCount = scanned,
                                totalCount = total,
                                progress = progress,
                                currentIpScanned = currentIp
                            )
                        }
                    },
                    onDeviceFound = { device ->
                        synchronized(foundDevices) {
                            foundDevices.removeAll { it.ipAddress == device.ipAddress }
                            foundDevices.add(device)
                            foundDevices.sortWith(
                                compareByDescending<ScannedDevice> { it.isGateway }
                                    .thenByDescending { it.isCurrentDevice }
                                    .thenBy { ipToLong(it.ipAddress) }
                            )
                        }
                        val sorted = synchronized(foundDevices) { foundDevices.toList() }
                        _uiState.update { it.copy(devices = sorted) }

                        // Persist device to local room database
                        viewModelScope.launch {
                            repository.saveDiscoveredDevice(device)
                        }
                    }
                )

                val elapsed = (System.currentTimeMillis() - scanStartTime) / 1000
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        progress = 1f,
                        scanDurationSeconds = elapsed
                    )
                }

                // Record scan history
                repository.recordScanHistory(
                    networkSsid = details.ssid,
                    gatewayIp = details.gatewayIp,
                    subnetRange = "$basePrefix.$startHost - $endHost",
                    devicesCount = foundDevices.size,
                    durationSeconds = elapsed
                )

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        alertMessage = "Pemindaian terhenti: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun stopSubnetScan() {
        scanJob?.cancel()
        repository.stopScan()
        _uiState.update { it.copy(isScanning = false) }
    }

    fun selectDevice(device: ScannedDevice?) {
        _uiState.update { it.copy(selectedDevice = device) }
    }

    fun updateDeviceAliasAndNotes(ip: String, alias: String?, notes: String?) {
        viewModelScope.launch {
            repository.updateAliasAndNotes(ip, alias, notes)
            // Update in active scan list if present
            _uiState.update { state ->
                val updated = state.devices.map {
                    if (it.ipAddress == ip) it.copy(customAlias = alias, notes = notes) else it
                }
                val sel = if (state.selectedDevice?.ipAddress == ip) {
                    state.selectedDevice?.copy(customAlias = alias, notes = notes)
                } else state.selectedDevice
                state.copy(devices = updated, selectedDevice = sel)
            }
        }
    }

    fun toggleFavorite(ip: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(ip, isFavorite)
            _uiState.update { state ->
                val updated = state.devices.map {
                    if (it.ipAddress == ip) it.copy(isFavorite = isFavorite) else it
                }
                val sel = if (state.selectedDevice?.ipAddress == ip) {
                    state.selectedDevice?.copy(isFavorite = isFavorite)
                } else state.selectedDevice
                state.copy(devices = updated, selectedDevice = sel)
            }
        }
    }

    fun deleteDevice(ip: String) {
        viewModelScope.launch {
            repository.deleteDevice(ip)
            _uiState.update { state ->
                val updated = state.devices.filterNot { it.ipAddress == ip }
                state.copy(devices = updated, selectedDevice = null)
            }
        }
    }

    fun clearAlertMessage() {
        _uiState.update { it.copy(alertMessage = null) }
    }

    // --- Port Scanner Actions ---
    fun startPortScan(ip: String) {
        portScanJob?.cancel()
        _portScanState.update {
            PortScanState(
                targetIp = ip,
                isScanning = true,
                openPorts = emptyList(),
                currentPort = 0,
                progress = 0f
            )
        }

        portScanJob = viewModelScope.launch {
            val ports = mutableListOf<OpenPortInfo>()
            val total = PortScanner.COMMON_PORTS.size

            PortScanner.COMMON_PORTS.forEachIndexed { index, def ->
                _portScanState.update {
                    it.copy(
                        currentPort = def.port,
                        progress = (index + 1).toFloat() / total.toFloat()
                    )
                }
                val result = PortScanner.checkPort(ip, def.port, timeoutMs = 350)
                if (result.isOpen) {
                    ports.add(result)
                    _portScanState.update { it.copy(openPorts = ports.toList()) }
                }
            }

            _portScanState.update { it.copy(isScanning = false, progress = 1f) }
        }
    }

    fun stopPortScan() {
        portScanJob?.cancel()
        _portScanState.update { it.copy(isScanning = false) }
    }

    // --- Ping Tool Actions ---
    fun startPing(ip: String, count: Int = 10) {
        pingJob?.cancel()
        _pingState.update {
            PingState(
                targetIp = ip,
                isRunning = true,
                results = emptyList(),
                stats = PingStats()
            )
        }

        pingJob = viewModelScope.launch {
            val list = mutableListOf<PingResult>()
            for (seq in 1..count) {
                if (!isActive) break
                val res = PingTester.ping(ip, timeoutMs = 1000, sequence = seq)
                list.add(res)
                val stats = PingTester.calculateStats(list)
                _pingState.update {
                    it.copy(results = list.toList(), stats = stats)
                }
                delay(400)
            }
            _pingState.update { it.copy(isRunning = false) }
        }
    }

    fun stopPing() {
        pingJob?.cancel()
        _pingState.update { it.copy(isRunning = false) }
    }

    // --- Wake-on-LAN ---
    fun sendWakeOnLan(macAddress: String, targetIp: String = "255.255.255.255") {
        viewModelScope.launch {
            val success = NetworkUtils.sendWakeOnLan(macAddress, targetIp)
            val msg = if (success) {
                "Paket ajaib Wake-on-LAN berhasil dikirim ke $macAddress!"
            } else {
                "Gagal mengirim Wake-on-LAN (periksa format MAC address)."
            }
            _uiState.update { it.copy(alertMessage = msg) }
        }
    }

    fun exportResults(): String {
        return repository.exportToFormattedText(
            devices = _uiState.value.devices,
            networkInfo = _uiState.value.networkDetails
        )
    }

    private fun ipToLong(ip: String): Long {
        return try {
            val parts = ip.split(".")
            var result = 0L
            for (part in parts) {
                result = (result shl 8) or part.toLong()
            }
            result
        } catch (_: Exception) {
            0L
        }
    }
}
