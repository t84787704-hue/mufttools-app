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
    val statusText: String,
    val features: List<String>
)

object ToolRepository {
    val defaultTools = listOf(
        ToolItem(
            id = "pdf_scanner",
            title = "PDF Scanner & Tools",
            shortDescription = "Scan documents, merge, split, compress & convert PDFs 100% offline.",
            detailedDescription = "Complete 100% offline PDF suite. Turn physical documents into high-quality PDFs using on-device edge detection, auto-crop, image filtering, and PDF generation without internet.",
            category = "Document",
            badge = "Offline PDF",
            accentColor = CyanPrimary,
            icon = Icons.Default.PictureAsPdf,
            route = "pdf_scanner",
            statusText = "100% On-Device",
            features = listOf("100% Offline Document Scanner", "Camera & Gallery Document Scan", "PDF Merger & Splitter", "PDF Page Extractor")
        ),
        ToolItem(
            id = "bg_remover",
            title = "Background Remover",
            shortDescription = "Remove image background on-device using ML Kit Selfie Segmentation.",
            detailedDescription = "Instant on-device ML Kit background eraser for photo editing, transparent PNG creation, and product catalog designs. Works 100% offline with zero cloud latency.",
            category = "AI Image",
            badge = "ML Kit Offline",
            accentColor = VioletSecondary,
            icon = Icons.Default.AutoFixHigh,
            route = "bg_remover",
            statusText = "Offline Processing",
            features = listOf("On-Device ML Kit Subject Eraser", "100% Offline & Private Processing", "Transparent PNG Export", "Custom Background Color")
        ),
        ToolItem(
            id = "qr_scanner",
            title = "QR Scanner & Generator",
            shortDescription = "Scan any QR code or barcode offline & generate custom styled QRs.",
            detailedDescription = "Ultra-fast on-device ML Kit QR and Barcode scanner with flashlight support, scan history, and custom color QR generator for Wi-Fi, URLs, and text.",
            category = "Utility",
            badge = "Offline QR",
            accentColor = EmeraldTertiary,
            icon = Icons.Default.QrCodeScanner,
            route = "qr_scanner",
            statusText = "Instant Scanner",
            features = listOf("Instant Barcode & QR Scan", "100% Offline Camera & Image Scan", "Custom Color QR Generator", "Wi-Fi & Text QR Creation")
        ),
        ToolItem(
            id = "video_compressor",
            title = "Video Compressor",
            shortDescription = "Reduce video file size offline without losing quality.",
            detailedDescription = "Compress large MP4 and video files on-device using Android hardware codecs to save phone storage or share easily on messaging apps.",
            category = "Media",
            badge = "Hardware Codec",
            accentColor = AccentAmber,
            icon = Icons.Default.VideoLibrary,
            route = "video_compressor",
            statusText = "Local Transcoding",
            features = listOf("100% On-Device Hardware Transcoding", "Custom Resolution & Bitrate", "Save Storage Space", "Fast Local Export")
        ),
        ToolItem(
            id = "image_tools",
            title = "Image Tools",
            shortDescription = "Resize, crop, convert & compress images in 1 click offline.",
            detailedDescription = "Multi-purpose on-device photo editor and converter tool. Resize dimensions, convert JPG/WEBP/PNG, compress kilobytes, and crop aspect ratios locally.",
            category = "Graphics",
            badge = "5-in-1 Offline",
            accentColor = AccentRose,
            icon = Icons.Default.Crop,
            route = "image_tools",
            statusText = "Local Processing",
            features = listOf("100% Local Image Compression", "Format Converter (JPG/PNG/WEBP)", "Aspect Crop & Rotate", "Quality Adjustments")
        )
    )
}
