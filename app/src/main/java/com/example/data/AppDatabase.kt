package com.example.data.pdf

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScannedDocument::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scannedDocumentDao(): ScannedDocumentDao
}