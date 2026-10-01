package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OpenPortInfo
import com.example.data.model.ScannedDevice
import com.example.network.PingResult
import com.example.network.PingStats
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusOnline
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailBottomSheet(
    device: ScannedDevice,
    onDismiss: () -> Unit,
    onUpdateAliasAndNotes: (alias: String?, notes: String?) -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    // Port Scan
    isPortScanning: Boolean,
    portScanResults: List<OpenPortInfo>,
    portScanCurrentPort: Int,
    portScanProgress: Float,
    onStartPortScan: (ip: String) -> Unit,
    onStopPortScan: () -> Unit,
    // Ping
    isPinging: Boolean,
    pingResults: List<PingResult>,
    pingStats: PingStats,
    onStartPing: (ip: String) -> Unit,
    onStopPing: () -> Unit,
    // WOL
    onSendWakeOnLan: (mac: String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var customAlias by remember(device.customAlias) { mutableStateOf(device.customAlias ?: "") }
    var notes by remember(device.notes) { mutableStateOf(device.notes ?: "") }
    var isEditingDetails by remember { mutableStateOf(false) }

    var selectedToolTab by remember { mutableStateOf(0) } // 0 = Info, 1 = Port Scanner, 2 = Ping Test

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("device_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
        ) {
            // Header: Category, Name, Close & Favorite
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryIcon(category = device.category, size = 48.dp, iconSize = 26.dp)

                    Column {
                        Text(
                            text = device.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "${device.category.title} • ${device.vendor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = { onToggleFavorite(!device.isFavorite) }) {
                        Icon(
                            imageVector = if (device.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorit",
                            tint = if (device.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tool Tabs (Info, Port Scanner, Ping Test)
            PrimaryTabRow(
                selectedTabIndex = selectedToolTab,
                containerColor = Color.Transparent,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) }
            ) {
                Tab(
                    selected = selectedToolTab == 0,
                    onClick = { selectedToolTab = 0 },
                    text = { Text("Informasi") },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedToolTab == 1,
                    onClick = {
                        selectedToolTab = 1
                        if (portScanResults.isEmpty() && !isPortScanning) {
                            onStartPortScan(device.ipAddress)
                        }
                    },
                    text = { Text("Pindai Port") },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedToolTab == 2,
                    onClick = {
                        selectedToolTab = 2
                        if (pingResults.isEmpty() && !isPinging) {
                            onStartPing(device.ipAddress)
                        }
                    },
                    text = { Text("Tes Ping") },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            when (selectedToolTab) {
                0 -> DeviceInfoTab(
                    device = device,
                    customAlias = customAlias,
                    onAliasChange = { customAlias = it },
                    notes = notes,
                    onNotesChange = { notes = it },
                    isEditing = isEditingDetails,
                    onToggleEdit = {
                        if (isEditingDetails) {
                            onUpdateAliasAndNotes(
                                customAlias.takeIf { it.isNotBlank() },
                                notes.takeIf { it.isNotBlank() }
                            )
                            Toast.makeText(context, "Data perangkat tersimpan", Toast.LENGTH_SHORT).show()
                        }
                        isEditingDetails = !isEditingDetails
                    },
                    onCopy = { label, text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
                        Toast.makeText(context, "$label disalin ke clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onOpenWeb = {
                        val url = "http://${device.ipAddress}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Tidak dapat membuka browser", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSendWol = {
                        if (device.macAddress.isNotBlank()) {
                            onSendWakeOnLan(device.macAddress)
                        } else {
                            Toast.makeText(context, "Alamat MAC tidak tersedia untuk Wake-on-LAN", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                1 -> PortScannerTab(
                    ip = device.ipAddress,
                    isScanning = isPortScanning,
                    results = portScanResults,
                    currentPort = portScanCurrentPort,
                    progress = portScanProgress,
                    onStart = { onStartPortScan(device.ipAddress) },
                    onStop = onStopPortScan
                )

                2 -> PingTesterTab(
                    ip = device.ipAddress,
                    isRunning = isPinging,
                    results = pingResults,
                    stats = pingStats,
                    onStart = { onStartPing(device.ipAddress) },
                    onStop = onStopPing
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoTab(
    device: ScannedDevice,
    customAlias: String,
    onAliasChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    onCopy: (String, String) -> Unit,
    onOpenWeb: () -> Unit,
    onSendWol: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCopy("Info Perangkat", "${device.displayName}\nIP: ${device.ipAddress}\nMAC: ${device.macAddress}\nVendor: ${device.vendor}") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Info", fontSize = 12.sp)
                }

                if (device.macAddress.isNotBlank()) {
                    OutlinedButton(
                        onClick = onSendWol,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim WOL", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onOpenWeb,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Web", fontSize = 12.sp)
                }
            }
        }

        // Editable Custom Name & Notes
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kustomisasi & Catatan",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onToggleEdit, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Simpan" else "Ubah",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isEditing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customAlias,
                            onValueChange = onAliasChange,
                            label = { Text("Nama Panggilan (Alias)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = onNotesChange,
                            label = { Text("Catatan Perangkat") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onToggleEdit,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Simpan Perubahan")
                        }
                    } else {
                        if (customAlias.isNotBlank()) {
                            Text(
                                text = "Alias: $customAlias",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Catatan: $notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (customAlias.isBlank() && notes.isBlank()) {
                            Text(
                                text = "Ketuk ikon pensil untuk memberi nama khusus atau catatan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Technical Specs Grid
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Spesifikasi Jaringan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    SpecRow(label = "Alamat IP", value = device.ipAddress, onCopy = { onCopy("IP", device.ipAddress) })
                    if (device.macAddress.isNotBlank()) {
                        SpecRow(label = "Alamat MAC", value = device.macAddress, onCopy = { onCopy("MAC", device.macAddress) })
                    }
                    SpecRow(label = "Produsen / Vendor", value = device.vendor)
                    SpecRow(label = "Kategori Perangkat", value = device.category.title)
                    if (!device.hostname.isNullOrBlank()) {
                        SpecRow(label = "Nama Host (DNS)", value = device.hostname)
                    }
                    if (device.latencyMs >= 0) {
                        SpecRow(label = "Waktu Respon (Ping)", value = "${device.latencyMs} ms")
                    }
                    if (device.openPorts.isNotEmpty()) {
                        SpecRow(label = "Port Terdeteksi", value = device.openPorts.joinToString(", "))
                    }
                    SpecRow(label = "Pertama Dilihat", value = dateFormat.format(Date(device.firstSeen)))
                    SpecRow(label = "Terakhir Dilihat", value = dateFormat.format(Date(device.lastSeen)))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SpecRow(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            )
            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PortScannerTab(
    ip: String,
    isScanning: Boolean,
    results: List<OpenPortInfo>,
    currentPort: Int,
    progress: Float,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pemindaian Port Terbuka",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Target: $ip • ${results.size} port terbuka",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { if (isScanning) onStop() else onStart() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) MaterialTheme.colorScheme.error else CyanPrimary
                )
            ) {
                Text(if (isScanning) "Hentikan" else "Pindai Ulang")
            }
        }

        if (isScanning) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Memeriksa port $currentPort...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada port standar yang terbuka atau pemindaian belum dijalankan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results) { portInfo ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusOnline.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${portInfo.port}",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = StatusOnline,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
    }
}

@Composable
private fun PingTesterTab(
    ip: String,
    isRunning: Boolean,
    results: List<PingResult>,
    stats: PingStats,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Diagnostik Ping & Latensi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Target: $ip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { if (isRunning) onStop() else onStart() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else CyanPrimary
                )
            ) {
                Text(if (isRunning) "Berhenti" else "Mulai Tes")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "Terkirim", value = "${stats.sent}")
                StatItem(label = "Diterima", value = "${stats.received}")
                StatItem(label = "Min", value = "${stats.minMs} ms")
                StatItem(label = "Rata-rata", value = "${stats.avgMs.toInt()} ms")
                StatItem(label = "Max", value = "${stats.maxMs} ms")
                StatItem(label = "Hilang", value = "${stats.packetLossPercent}%")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ping Logs
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(results) { res ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                            text = "#${res.sequence} bytes=32 ke ${res.ip}",
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
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}
