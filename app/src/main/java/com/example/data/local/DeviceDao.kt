package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Upsert
    suspend fun upsertDevice(device: ScannedDeviceEntity)

    @Upsert
    suspend fun upsertDevices(devices: List<ScannedDeviceEntity>)

    @Query("SELECT * FROM scanned_devices ORDER BY lastSeenTimestamp DESC")
    fun getAllDevices(): Flow<List<ScannedDeviceEntity>>

    @Query("SELECT * FROM scanned_devices WHERE isFavorite = 1 ORDER BY ipAddress ASC")
    fun getFavoriteDevices(): Flow<List<ScannedDeviceEntity>>

    @Query("SELECT * FROM scanned_devices WHERE ipAddress = :ip LIMIT 1")
    suspend fun getDeviceByIp(ip: String): ScannedDeviceEntity?

    @Query("UPDATE scanned_devices SET customAlias = :alias, notes = :notes WHERE ipAddress = :ip")
    suspend fun updateAliasAndNotes(ip: String, alias: String?, notes: String?)

    @Query("UPDATE scanned_devices SET isFavorite = :isFavorite WHERE ipAddress = :ip")
    suspend fun updateFavorite(ip: String, isFavorite: Boolean)

    @Delete
    suspend fun deleteDevice(device: ScannedDeviceEntity)

    @Query("DELETE FROM scanned_devices")
    suspend fun clearAllDevices()
}
