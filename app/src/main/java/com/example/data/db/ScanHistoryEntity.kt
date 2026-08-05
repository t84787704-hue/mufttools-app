package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val filePath: String,
    val pageCount: Int,
    val fileSize: String,
    val timestamp: Long,
    val thumbnailPath: String? = null
)
