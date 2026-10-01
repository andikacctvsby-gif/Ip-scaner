package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.StatusOnline
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScannerRadarControl(
    isScanning: Boolean,
    progress: Float,
    scannedCount: Int,
    totalCount: Int,
    currentIp: String,
    deviceCount: Int,
    deepScanEnabled: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onToggleDeepScan: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Radar / Pulse Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .padding(4.dp)
            ) {
                RadarAnimation(isScanning = isScanning)

                // Center Icon / Count Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CyanPrimary.copy(alpha = 0.35f), MaterialTheme.colorScheme.surface)
                            )
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$deviceCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (deviceCount > 0) StatusOnline else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "DITEMUKAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Text & Progress
            if (isScanning) {
                Text(
                    text = "Memindai: $currentIp",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyanPrimary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$scannedCount / $totalCount host",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyanPrimary
                    )
                }
            } else {
                Text(
                    text = if (scannedCount > 0) "Pemindaian Selesai" else "Siap Memindai Jaringan",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (deviceCount > 0) "$deviceCount perangkat aktif terdeteksi" else "Tekan tombol untuk memindai semua IP lokal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row & Deep Scan Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Scan / Stop Primary Button
                Button(
                    onClick = {
                        if (isScanning) onStopScan() else onStartScan()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) MaterialTheme.colorScheme.error else CyanPrimary,
                        contentColor = if (isScanning) Color.White else DeepNavy
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scan_action_button")
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isScanning) "Hentikan" else "Pindai Jaringan",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isScanning) "Hentikan" else (if (scannedCount > 0) "Pindai Ulang" else "Mulai Pindai"),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Deep Scan Option Card
                FilterChip(
                    selected = deepScanEnabled,
                    onClick = { onToggleDeepScan(!deepScanEnabled) },
                    label = {
                        Text(
                            text = if (deepScanEnabled) "Pindai Mendalam" else "Pindai Cepat",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Opsi",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("deep_scan_toggle")
                )
            }
        }
    }
}

@Composable
fun RadarAnimation(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val sweepColor = CyanAccent
    val gridColor = CyanPrimary.copy(alpha = 0.25f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // Draw concentric sonar circles
        drawCircle(color = gridColor, radius = radius * 0.35f, center = center, style = Stroke(1.5f))
        drawCircle(color = gridColor, radius = radius * 0.70f, center = center, style = Stroke(1.5f))
        drawCircle(color = gridColor, radius = radius * 0.98f, center = center, style = Stroke(2f))

        // Crosshairs
        drawLine(
            color = gridColor,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1f
        )
        drawLine(
            color = gridColor,
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1f
        )

        // Radar Sweep Line when scanning
        if (isScanning) {
            val rad = Math.toRadians(angle.toDouble())
            val endX = center.x + radius * cos(rad).toFloat()
            val endY = center.y + radius * sin(rad).toFloat()

            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(sweepColor, sweepColor.copy(alpha = 0.1f)),
                    center = center,
                    radius = radius
                ),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Outer pulse circle
            drawCircle(
                color = sweepColor.copy(alpha = pulseAlpha),
                radius = radius * 0.98f,
                center = center,
                style = Stroke(3f)
            )
        }
    }
}
