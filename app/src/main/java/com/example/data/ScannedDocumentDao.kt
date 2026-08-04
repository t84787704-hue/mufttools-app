package com.example.data.pdf

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedDocumentDao {

    @Insert
    suspend fun insert(document: ScannedDocument): Long

    @Delete
    suspend fun delete(document: ScannedDocument)

    @Query("SELECT * FROM scanned_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<ScannedDocument>>

    @Query("SELECT * FROM scanned_documents WHERE id = :id LIMIT 1")
    suspend fun getDocument(id: Long): ScannedDocument?

    @Query("DELETE FROM scanned_documents")
    suspend fun deleteAll()
}