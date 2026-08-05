package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QrCodeDao {
    @Query("SELECT * FROM qr_code_history ORDER BY timestamp DESC")
    fun getAllQrCodes(): Flow<List<QrCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrCode(qrCode: QrCodeEntity)

    @Query("DELETE FROM qr_code_history WHERE id = :id")
    suspend fun deleteQrCode(id: String)

    @Query("DELETE FROM qr_code_history")
    suspend fun clearAll()
}
