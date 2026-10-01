package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val networkSsid: String,
    val gatewayIp: String,
    val subnetRange: String,
    val devicesFoundCount: Int,
    val durationSeconds: Long
)
