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
            shortDescription = "Scan documents, merge, split, compress & convert PDFs 100% offline.",
            detailedDescription = "Complete 100% offline PDF suite. Turn physical documents into high-quality PDFs using on-device ML Kit OCR, auto-crop, edge detection, and digital signature without internet.",
            category = "Document",
            badge = "Offline ML",
            accentColor = CyanPrimary,
            icon = Icons.Default.PictureAsPdf,
            route = "pdf_scanner",
            rating = "4.9★",
            usageCount = "1.2M+ uses",
            features = listOf("100% Offline ML Kit OCR", "HD Camera Document Scanner", "PDF Merger & Splitter", "PDF Password Protection")
        ),
        ToolItem(
            id = "bg_remover",
            title = "Background Remover",
            shortDescription = "Remove image background on-device using ML Kit Selfie Segmentation.",
            detailedDescription = "Instant on-device ML Kit background eraser for photo editing, transparent PNG creation, and product catalog designs. Works 100% offline with zero cloud latency.",
            category = "AI Image",
            badge = "On-Device ML",
            accentColor = VioletSecondary,
            icon = Icons.Default.AutoFixHigh,
            route = "bg_remover",
            rating = "4.8★",
            usageCount = "850K+ uses",
            features = listOf("On-Device ML Kit Subject Eraser", "100% Offline & Private Processing", "Transparent PNG Export", "Custom Background Replacement")
        ),
        ToolItem(
            id = "qr_scanner",
            title = "QR Scanner & Generator",
            shortDescription = "Scan any QR code or barcode offline & generate custom styled QRs.",
            detailedDescription = "Ultra-fast on-device ML Kit QR and Barcode scanner with flashlight support, scan history, and custom color QR generator for Wi-Fi, URLs, and contacts.",
            category = "Utility",
            badge = "Offline QR",
            accentColor = EmeraldTertiary,
            icon = Icons.Default.QrCodeScanner,
            route = "qr_scanner",
            rating = "4.9★",
            usageCount = "2.1M+ uses",
            features = listOf("Instant ML Kit Barcode Scan", "100% Offline Camera & Gallery Scan", "Custom Logo QR Generator", "Wi-Fi & vCard QR Creation")
        ),
        ToolItem(
            id = "video_compressor",
            title = "Video Compressor",
            shortDescription = "Reduce video file size offline without losing 1080p / 4K quality.",
            detailedDescription = "Compress large MP4, MOV, and AVI video files on-device using hardware codecs to save phone storage or share easily on WhatsApp and Social Media.",
            category = "Media",
            badge = "Local Codec",
            accentColor = AccentAmber,
            icon = Icons.Default.VideoLibrary,
            route = "video_compressor",
            rating = "4.7★",
            usageCount = "950K+ uses",
            features = listOf("100% On-Device Hardware Transcoding", "Custom Resolution & Bitrate", "Batch Video Compression", "Format Conversion (MP4/MOV)")
        ),
        ToolItem(
            id = "image_tools",
            title = "Image Tools",
            shortDescription = "Resize, crop, convert & compress images in 1 click offline.",
            detailedDescription = "Multi-purpose on-device photo editor and converter tool. Resize dimensions, convert JPG/WEBP/PNG, compress kilobytes, and edit aspect ratios locally.",
            category = "Graphics",
            badge = "5-in-1 Offline",
            accentColor = AccentRose,
            icon = Icons.Default.Crop,
            route = "image_tools",
            rating = "4.8★",
            usageCount = "1.5M+ uses",
            features = listOf("100% Local Kilobyte Compression", "Format Converter (JPG/PNG/WEBP)", "Batch Aspect Crop", "EXIF Data Cleaner")
        )
    )
}
