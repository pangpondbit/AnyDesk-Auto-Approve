package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoClickLogDao {
    @Query("SELECT * FROM auto_click_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogsFlow(): Flow<List<AutoClickLog>>

    @Query("SELECT * FROM auto_click_logs ORDER BY timestamp DESC LIMIT 100")
    suspend fun getAllLogs(): List<AutoClickLog>

    @Insert
    suspend fun insertLog(log: AutoClickLog)

    @Query("DELETE FROM auto_click_logs")
    suspend fun clearAllLogs()
}
