package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Insert
    suspend fun insertHistory(history: ScanHistoryEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<ScanHistoryEntity>>

    @Query("DELETE FROM scan_history")
    suspend fun clearHistory()
}
