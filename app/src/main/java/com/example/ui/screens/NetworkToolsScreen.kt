package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.NetworkUtils
import com.example.network.PingResult
import com.example.network.PingTester
import com.example.network.PortScanner
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusOnline
import com.example.ui.viewmodel.ScannerViewModel
import kotlinx.coroutines.launch

@Composable
fun NetworkToolsScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var selectedToolIndex by remember { mutableStateOf(0) }

    // Standalone Ping Tool State
    var pingTargetIp by remember { mutableStateOf(uiState.networkDetails.gatewayIp.takeIf { it != "0.0.0.0" } ?: "8.8.8.8") }
    var pingRunning by remember { mutableStateOf(false) }
    val pingLog = remember { mutableStateListOf<PingResult>() }

    // Standalone Port Scanner State
    var portTargetIp by remember { mutableStateOf(uiState.networkDetails.gatewayIp.takeIf { it != "0.0.0.0" } ?: "192.168.1.1") }
    var portScanning by remember { mutableStateOf(false) }
    var currentPortScanning by remember { mutableStateOf(0) }
    val openPortsFound = remember { mutableStateListOf<com.example.data.model.OpenPortInfo>() }

    // Standalone WOL State
    var wolMac by remember { mutableStateOf("") }

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
                        text = "Alat Diagnostik Jaringan",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Perangkat pengujian latensi, pemindaian port, dan Wake-on-LAN",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tools Navigation Tabs
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedToolIndex == 0,
                        onClick = { selectedToolIndex = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Tes Ping", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = selectedToolIndex == 1,
                        onClick = { selectedToolIndex = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Pindai Port", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = selectedToolIndex == 2,
                        onClick = { selectedToolIndex = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("Wake on LAN", fontSize = 12.sp)
                    }
                }
            }

            when (selectedToolIndex) {
                // 1. PING TOOL
                0 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Uji Respon & Latensi Host",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                OutlinedTextField(
                                    value = pingTargetIp,
                                    onValueChange = { pingTargetIp = it },
                                    label = { Text("Alamat IP / Domain Target") },
                                    placeholder = { Text("misal: 192.168.1.1 atau 8.8.8.8") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            if (pingRunning) {
                                                pingRunning = false
                                            } else {
                                                pingRunning = true
                                                pingLog.clear()
                                                coroutineScope.launch {
                                                    for (seq in 1..8) {
                                                        if (!pingRunning) break
                                                        val res = PingTester.ping(pingTargetIp.trim(), sequence = seq)
                                                        pingLog.add(res)
                                                        kotlinx.coroutines.delay(400)
                                                    }
                                                    pingRunning = false
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (pingRunning) MaterialTheme.colorScheme.error else CyanPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (pingRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (pingRunning) "Hentikan" else "Mulai Ping")
                                    }

                                    OutlinedButton(
                                        onClick = { pingLog.clear() },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Bersihkan")
                                    }
                                }
                            }
                        }
                    }

                    if (pingLog.isNotEmpty()) {
                        item {
                            val stats = PingTester.calculateStats(pingLog)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Min", style = MaterialTheme.typography.labelSmall)
                                        Text("${stats.minMs} ms", fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Avg", style = MaterialTheme.typography.labelSmall)
                                        Text("${stats.avgMs.toInt()} ms", fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Max", style = MaterialTheme.typography.labelSmall)
                                        Text("${stats.maxMs} ms", fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Loss", style = MaterialTheme.typography.labelSmall)
                                        Text("${stats.packetLossPercent}%", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        items(pingLog) { res ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${res.sequence} ke ${res.ip}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                    if (res.isSuccess) {
                                        Text(
                                            text = "waktu=${res.timeMs} ms",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = StatusOnline
                                        )
                                    } else {
                                        Text(
                                            text = "Request Timed Out",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. PORT SCANNER TOOL
                1 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Pemindai Port Terbuka",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                OutlinedTextField(
                                    value = portTargetIp,
                                    onValueChange = { portTargetIp = it },
                                    label = { Text("Alamat IP Sasaran") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            if (portScanning) {
                                                portScanning = false
                                            } else {
                                                portScanning = true
                                                openPortsFound.clear()
                                                coroutineScope.launch {
                                                    val results = PortScanner.scanCommonPorts(
                                                        ip = portTargetIp.trim(),
                                                        onPortChecked = { openPort ->
                                                            openPortsFound.add(openPort)
                                                        }
                                                    )
                                                    portScanning = false
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (portScanning) MaterialTheme.colorScheme.error else CyanPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (portScanning) Icons.Default.Stop else Icons.Default.Radar,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (portScanning) "Hentikan" else "Pindai Port Standar")
                                    }

                                    OutlinedButton(
                                        onClick = { openPortsFound.clear() },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Bersihkan")
                                    }
                                }
                            }
                        }
                    }

                    if (openPortsFound.isEmpty() && !portScanning) {
                        item {
                            Text(
                                text = "Hasil port terbuka akan ditampilkan di sini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(openPortsFound) { portInfo ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = StatusOnline.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${portInfo.port}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = StatusOnline,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = portInfo.serviceName,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = portInfo.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "TERBUKA",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = StatusOnline
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. WAKE ON LAN TOOL
                2 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Kirim Paket Ajaib (Wake-on-LAN)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Nyalakan komputer atau server yang mendukung WOL di jaringan lokal dengan memasukkan alamat MAC.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = wolMac,
                                    onValueChange = { wolMac = it },
                                    label = { Text("Alamat MAC Target") },
                                    placeholder = { Text("misal: 00:1A:2B:3C:4D:5E") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (wolMac.isBlank()) {
                                            Toast.makeText(context, "Masukkan alamat MAC terlebih dahulu", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.sendWakeOnLan(wolMac.trim())
                                            Toast.makeText(context, "Paket WOL dikirim ke $wolMac", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Kirim Paket Bangun (Magic Packet)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
