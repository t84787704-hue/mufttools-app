package com.example.data.pdf

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_history")
data class PdfHistory(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val filePath: String,

    val thumbnailPath: String = "",

    val pageCount: Int,

    val fileSize: Long,

    val createdAt: Long = System.currentTimeMillis()

)