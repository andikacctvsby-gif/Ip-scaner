package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceCategory
import com.example.ui.components.DeviceDetailBottomSheet
import com.example.ui.components.DeviceListItem
import com.example.ui.viewmodel.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedDevicesScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    val savedDevices by viewModel.savedDevices.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val portScanState by viewModel.portScanState.collectAsState()
    val pingState by viewModel.pingState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DeviceCategory?>(null) }
    var onlyFavorites by remember { mutableStateOf(false) }

    val filteredList = remember(savedDevices, searchQuery, selectedCategory, onlyFavorites) {
        savedDevices.filter { dev ->
            val matchesFav = !onlyFavorites || dev.isFavorite
            val matchesCat = selectedCategory == null || dev.category == selectedCategory
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                    dev.ipAddress.contains(q) ||
                    dev.macAddress.lowercase().contains(q) ||
                    dev.vendor.lowercase().contains(q) ||
                    dev.displayName.lowercase().contains(q) ||
                    (dev.notes?.lowercase()?.contains(q) == true)
            matchesFav && matchesCat && matchesSearch
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
            item {
                Column {
                    Text(
                        text = "Perangkat Tersimpan",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Daftar perangkat yang pernah terhubung beserta alias dan catatan Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search & Filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama, IP, MAC...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        FilterChip(
                            selected = onlyFavorites,
                            onClick = { onlyFavorites = !onlyFavorites },
                            label = { Text("Hanya Favorit") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (onlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Semua Kategori") }
                        )

                        DeviceCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text(cat.title) }
                            )
                        }
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (savedDevices.isEmpty()) "Belum ada perangkat yang tersimpan.\nLakukan pemindaian jaringan terlebih dahulu."
                                    else "Tidak ada perangkat yang cocok dengan kriteria pencarian.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.ipAddress }) { device ->
                    DeviceListItem(
                        device = device,
                        onClick = { viewModel.selectDevice(device) },
                        onToggleFavorite = { viewModel.toggleFavorite(device.ipAddress, it) }
                    )
                }
            }
        }

        uiState.selectedDevice?.let { device ->
            DeviceDetailBottomSheet(
                device = device,
                onDismiss = { viewModel.selectDevice(null) },
                onUpdateAliasAndNotes = { alias, notes ->
                    viewModel.updateDeviceAliasAndNotes(device.ipAddress, alias, notes)
                },
                onToggleFavorite = { viewModel.toggleFavorite(device.ipAddress, it) },
                isPortScanning = portScanState.isScanning,
                portScanResults = portScanState.openPorts,
                portScanCurrentPort = portScanState.currentPort,
                portScanProgress = portScanState.progress,
                onStartPortScan = { viewModel.startPortScan(it) },
                onStopPortScan = { viewModel.stopPortScan() },
                isPinging = pingState.isRunning,
                pingResults = pingState.results,
                pingStats = pingState.stats,
                onStartPing = { viewModel.startPing(it) },
                onStopPing = { viewModel.stopPing() },
                onSendWakeOnLan = { viewModel.sendWakeOnLan(it) }
            )
        }
    }
}
