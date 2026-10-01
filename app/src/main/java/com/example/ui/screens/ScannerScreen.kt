package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceCategory
import com.example.data.model.ScannedDevice
import com.example.ui.components.DeviceDetailBottomSheet
import com.example.ui.components.DeviceListItem
import com.example.ui.components.NetworkHeaderCard
import com.example.ui.components.ScannerRadarControl
import com.example.ui.viewmodel.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val portScanState by viewModel.portScanState.collectAsState()
    val pingState by viewModel.pingState.collectAsState()

    // Filter devices based on category filter and search query
    val filteredDevices = remember(uiState.devices, uiState.searchQuery, uiState.selectedCategoryFilter) {
        uiState.devices.filter { device ->
            val matchesCategory = uiState.selectedCategoryFilter == null || device.category == uiState.selectedCategoryFilter
            val query = uiState.searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                    device.ipAddress.lowercase().contains(query) ||
                    device.macAddress.lowercase().contains(query) ||
                    device.vendor.lowercase().contains(query) ||
                    (device.displayName.lowercase().contains(query)) ||
                    (device.hostname?.lowercase()?.contains(query) == true) ||
                    (device.notes?.lowercase()?.contains(query) == true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Network Details Card
            item {
                NetworkHeaderCard(
                    networkDetails = uiState.networkDetails,
                    onRefresh = { viewModel.refreshNetworkInfo() }
                )
            }

            // Radar Control & Scanning Center
            item {
                ScannerRadarControl(
                    isScanning = uiState.isScanning,
                    progress = uiState.progress,
                    scannedCount = uiState.scannedCount,
                    totalCount = uiState.totalCount,
                    currentIp = uiState.currentIpScanned,
                    deviceCount = uiState.devices.size,
                    deepScanEnabled = uiState.deepScanEnabled,
                    onStartScan = { viewModel.startSubnetScan() },
                    onStopScan = { viewModel.stopSubnetScan() },
                    onToggleDeepScan = { viewModel.toggleDeepScan(it) }
                )
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Cari IP, nama, vendor, atau MAC...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_field")
                    )

                    // Category Filter Chips Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("Semua (${uiState.devices.size})") }
                        )

                        DeviceCategory.entries.forEach { cat ->
                            val count = uiState.devices.count { it.category == cat }
                            if (count > 0 || uiState.devices.isEmpty()) {
                                FilterChip(
                                    selected = uiState.selectedCategoryFilter == cat,
                                    onClick = {
                                        viewModel.setCategoryFilter(
                                            if (uiState.selectedCategoryFilter == cat) null else cat
                                        )
                                    },
                                    label = { Text("${cat.title} ($count)") }
                                )
                            }
                        }
                    }
                }
            }

            // Results Header with Share / Export
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perangkat Terdeteksi (${filteredDevices.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    if (uiState.devices.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val report = viewModel.exportResults()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, report)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Bagikan Hasil Pemindaian IP")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.testTag("share_results_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Bagikan Hasil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Discovered Device Cards
            if (filteredDevices.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (uiState.isScanning) Icons.Default.Radar else Icons.Default.DevicesOther,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (uiState.isScanning) "Sedang mencari perangkat dalam subnet..."
                                    else if (uiState.devices.isNotEmpty()) "Tidak ada perangkat yang cocok dengan filter"
                                    else "Tekan 'Mulai Pindai' untuk menemukan semua perangkat di WiFi ini",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredDevices, key = { it.ipAddress }) { device ->
                    DeviceListItem(
                        device = device,
                        onClick = { viewModel.selectDevice(device) },
                        onToggleFavorite = { viewModel.toggleFavorite(device.ipAddress, it) }
                    )
                }
            }
        }

        // Selected Device Detail BottomSheet
        uiState.selectedDevice?.let { device ->
            DeviceDetailBottomSheet(
                device = device,
                onDismiss = { viewModel.selectDevice(null) },
                onUpdateAliasAndNotes = { alias, notes ->
                    viewModel.updateDeviceAliasAndNotes(device.ipAddress, alias, notes)
                },
                onToggleFavorite = { viewModel.toggleFavorite(device.ipAddress, it) },
                // Port Scan
                isPortScanning = portScanState.isScanning,
                portScanResults = portScanState.openPorts,
                portScanCurrentPort = portScanState.currentPort,
                portScanProgress = portScanState.progress,
                onStartPortScan = { viewModel.startPortScan(it) },
                onStopPortScan = { viewModel.stopPortScan() },
                // Ping
                isPinging = pingState.isRunning,
                pingResults = pingState.results,
                pingStats = pingState.stats,
                onStartPing = { viewModel.startPing(it) },
                onStopPing = { viewModel.stopPing() },
                // WOL
                onSendWakeOnLan = { viewModel.sendWakeOnLan(it) }
            )
        }
    }
}
