package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.Color

object ToolRepository {

    val defaultTools = listOf(

        ToolItem(
            id = "pdf_scanner",
            title = "PDF Scanner",
            shortDescription = "Scan documents to PDF",
            detailedDescription = "Offline PDF Scanner",
            category = "Document",
            badge = "PDF",
            accentColor = Color(0xFF00BCD4),
            icon = Icons.Default.Build,
            route = "pdf_scanner",
            statusText = "Ready",
            features = listOf(
                "Scan PDF",
                "Crop Document",
                "Save PDF"
            )
        ),

        ToolItem(
            id = "qr_scanner",
            title = "QR Scanner",
            shortDescription = "Scan QR Codes",
            detailedDescription = "Offline QR Scanner",
            category = "Utility",
            badge = "QR",
            accentColor = Color(0xFF4CAF50),
            icon = Icons.Default.Build,
            route = "qr_scanner",
            statusText = "Ready",
            features = listOf(
                "Scan QR",
                "Generate QR"
            )
        ),

        ToolItem(
            id = "video_compressor",
            title = "Video Compressor",
            shortDescription = "Compress videos",
            detailedDescription = "Offline Video Compressor",
            category = "Media",
            badge = "Video",
            accentColor = Color(0xFFFF9800),
            icon = Icons.Default.Build,
            route = "video_compressor",
            statusText = "Ready",
            features = listOf(
                "Compress Video"
            )
        ),

        ToolItem(
            id = "image_tools",
            title = "Image Tools",
            shortDescription = "Crop, Resize & Compress Images",
            detailedDescription = "Offline Image Tools",
            category = "Graphics",
            badge = "Image",
            accentColor = Color(0xFFE91E63),
            icon = Icons.Default.Build,
            route = "image_tools",
            statusText = "Ready",
            features = listOf(
                "Crop",
                "Resize",
                "Compress"
            )
        )
    )
}