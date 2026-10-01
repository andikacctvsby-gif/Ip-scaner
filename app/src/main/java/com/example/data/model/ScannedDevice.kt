package com.example.data.model

data class ScannedDevice(
    val ipAddress: String,
    val macAddress: String = "",
    val vendor: String = "Tidak diketahui",
    val category: DeviceCategory = DeviceCategory.UNKNOWN,
    val hostname: String? = null,
    val latencyMs: Long = -1L,
    val openPorts: List<Int> = emptyList(),
    val isCurrentDevice: Boolean = false,
    val isGateway: Boolean = false,
    val customAlias: String? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = when {
            !customAlias.isNullOrBlank() -> customAlias
            !hostname.isNullOrBlank() && hostname != ipAddress -> hostname
            isGateway -> "Router / Gateway"
            isCurrentDevice -> "Perangkat Ini (HP Ini)"
            vendor != "Tidak diketahui" -> "$vendor ($ipAddress)"
            else -> "Perangkat ($ipAddress)"
        }

    val isOnline: Boolean
        get() = latencyMs >= 0
}

data class OpenPortInfo(
    val port: Int,
    val serviceName: String,
    val description: String,
    val isOpen: Boolean = true
)
