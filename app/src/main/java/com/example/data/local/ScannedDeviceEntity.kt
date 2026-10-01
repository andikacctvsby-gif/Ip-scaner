package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DeviceCategory
import com.example.data.model.ScannedDevice

@Entity(tableName = "scanned_devices")
data class ScannedDeviceEntity(
    @PrimaryKey
    val ipAddress: String,
    val macAddress: String,
    val vendor: String,
    val categoryName: String,
    val hostname: String?,
    val customAlias: String?,
    val notes: String?,
    val isFavorite: Boolean,
    val firstSeenTimestamp: Long,
    val lastSeenTimestamp: Long,
    val lastLatencyMs: Long,
    val openPortsString: String // Comma separated ports e.g. "80,443"
) {
    fun toScannedDevice(isCurrentDevice: Boolean = false, isGateway: Boolean = false): ScannedDevice {
        val ports = if (openPortsString.isBlank()) emptyList() else openPortsString.split(",").mapNotNull { it.trim().toIntOrNull() }
        return ScannedDevice(
            ipAddress = ipAddress,
            macAddress = macAddress,
            vendor = vendor,
            category = DeviceCategory.fromString(categoryName),
            hostname = hostname,
            latencyMs = lastLatencyMs,
            openPorts = ports,
            isCurrentDevice = isCurrentDevice,
            isGateway = isGateway,
            customAlias = customAlias,
            notes = notes,
            isFavorite = isFavorite,
            firstSeen = firstSeenTimestamp,
            lastSeen = lastSeenTimestamp
        )
    }

    companion object {
        fun fromScannedDevice(device: ScannedDevice): ScannedDeviceEntity {
            return ScannedDeviceEntity(
                ipAddress = device.ipAddress,
                macAddress = device.macAddress,
                vendor = device.vendor,
                categoryName = device.category.name,
                hostname = device.hostname,
                customAlias = device.customAlias,
                notes = device.notes,
                isFavorite = device.isFavorite,
                firstSeenTimestamp = device.firstSeen,
                lastSeenTimestamp = device.lastSeen,
                lastLatencyMs = device.latencyMs,
                openPortsString = device.openPorts.joinToString(",")
            )
        }
    }
}
