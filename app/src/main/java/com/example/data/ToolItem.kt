package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.VioletSecondary

data class ToolItem(
    val id: String,
    val title: String,
    val shortDescription: String,
    val detailedDescription: String,
    val category: String,
    val badge: String,
    val accentColor: Color,
    val icon: ImageVector,
    val route: String,
    val rating: String,
    val usageCount: String,
    val features: List<String>
)

object ToolRepository {
    val defaultTools = listOf(
        ToolItem(
            id = "pdf_scanner",
            title = "PDF Scanner & Tools",
            shortDescription = "Scan documents, merge, split, compress & convert PDFs easily.",
            detailedDescription = "Complete PDF suite. Turn physical documents into high-quality PDFs with OCR, auto-crop, edge detection, and digital signature.",
            category = "Document",
            badge = "Popular",
            accentColor = CyanPrimary,
            icon = Icons.Default.PictureAsPdf,
            route = "pdf_scanner",
            rating = "4.9★",
            usageCount = "1.2M+ uses",
            features = listOf("HD Camera Document Scanner", "PDF Merger & Splitter", "OCR Text Extraction", "PDF Password Protection")
        ),
        ToolItem(
            id = "bg_remover",
            title = "Background Remover",
            shortDescription = "Remove image background automatically using AI in seconds.",
            detailedDescription = "Instant AI-powered background eraser for photo editing, transparent PNG creation, and product catalog designs.",
            category = "AI Image",
            badge = "AI Powered",
            accentColor = VioletSecondary,
            icon = Icons.Default.AutoFixHigh,
            route = "bg_remover",
            rating = "4.8★",
            usageCount = "850K+ uses",
            features = listOf("Automatic AI Background Eraser", "Transparent PNG Export", "Custom Background Replacement", "Batch Eraser")
        ),
        ToolItem(
            id = "qr_scanner",
            title = "QR Scanner & Generator",
            shortDescription = "Scan any QR code or barcode & generate custom styled QRs.",
            detailedDescription = "Ultra-fast QR and Barcode scanner with flashlight support, scan history, and custom color QR generator for Wi-Fi, URLs, and contacts.",
            category = "Utility",
            badge = "Fast",
            accentColor = EmeraldTertiary,
            icon = Icons.Default.QrCodeScanner,
            route = "qr_scanner",
            rating = "4.9★",
            usageCount = "2.1M+ uses",
            features = listOf("Instant Camera & Gallery Scan", "Custom Logo QR Generator", "Wi-Fi & vCard QR Creation", "Scan History & Export")
        ),
        ToolItem(
            id = "video_compressor",
            title = "Video Compressor",
            shortDescription = "Reduce video file size without losing 1080p / 4K quality.",
            detailedDescription = "Compress large MP4, MOV, and AVI video files to save phone storage or share easily on WhatsApp, Email, and Social Media.",
            category = "Media",
            badge = "HD Saver",
            accentColor = AccentAmber,
            icon = Icons.Default.VideoLibrary,
            route = "video_compressor",
            rating = "4.7★",
            usageCount = "950K+ uses",
            features = listOf("Custom Resolution & Bitrate", "Batch Video Compression", "Format Conversion (MP4/MOV)", "Audio Track Removal")
        ),
        ToolItem(
            id = "image_tools",
            title = "Image Tools",
            shortDescription = "Resize, crop, convert & compress images in 1 click.",
            detailedDescription = "Multi-purpose photo editor and converter tool. Resize dimensions, convert JPG/WEBP/PNG, compress kilobytes, and edit aspect ratios.",
            category = "Graphics",
            badge = "5-in-1",
            accentColor = AccentRose,
            icon = Icons.Default.Crop,
            route = "image_tools",
            rating = "4.8★",
            usageCount = "1.5M+ uses",
            features = listOf("Kb/Mb Image Compression", "Format Converter (JPG/PNG/WEBP)", "Batch Aspect Crop", "EXIF Data Cleaner")
        )
    )
}
