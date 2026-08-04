package com.example.data.pdf

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfHistoryDao {

    @Query("SELECT * FROM pdf_history ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PdfHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PdfHistory): Long

    @Delete
    suspend fun delete(item: PdfHistory)

    @Query("DELETE FROM pdf_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM pdf_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PdfHistory?

    @Query("UPDATE pdf_history SET title = :newTitle WHERE id = :id")
    suspend fun rename(id: Long, newTitle: String)

    @Query("DELETE FROM pdf_history")
    suspend fun clearAll()
}