package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "qr_code_history")
data class QrCodeEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: String, // "URL", "Text", "Contact", "Wi-Fi", "Email", "SMS", "Phone"
    val isGenerated: Boolean, // true = generated, false = scanned
    val timestamp: Long = System.currentTimeMillis(),
    val title: String? = null
)
