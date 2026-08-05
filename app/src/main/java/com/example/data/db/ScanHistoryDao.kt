package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity)

    @Query("UPDATE scan_history SET title = :newTitle WHERE id = :id")
    suspend fun renameScan(id: String, newTitle: String)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScan(id: String)
}
