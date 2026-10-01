package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.DeviceCategory

@Composable
fun CategoryIcon(
    category: DeviceCategory,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (category) {
        DeviceCategory.ROUTER -> Icons.Default.Router
        DeviceCategory.SMARTPHONE -> Icons.Default.Smartphone
        DeviceCategory.COMPUTER -> Icons.Default.Computer
        DeviceCategory.IOT -> Icons.Default.Memory
        DeviceCategory.SMART_TV -> Icons.Default.Tv
        DeviceCategory.PRINTER -> Icons.Default.Print
        DeviceCategory.CAMERA -> Icons.Default.Videocam
        DeviceCategory.SERVER_NAS -> Icons.Default.Storage
        DeviceCategory.GAMING -> Icons.Default.SportsEsports
        DeviceCategory.UNKNOWN -> Icons.Default.DevicesOther
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(category.color.copy(alpha = 0.15f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.title,
            tint = category.color,
            modifier = Modifier.size(iconSize)
        )
    }
}
