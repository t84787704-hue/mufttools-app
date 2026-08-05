package com.mufttools.scanner.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import com.mufttools.scanner.util.ScanFilter
import java.util.UUID

data class ScannedPage(
    val id: String = UUID.randomUUID().toString(),
    val originalBitmap: Bitmap,
    val croppedBitmap: Bitmap = originalBitmap,
    val corners: List<Offset> = emptyList(),
    val filter: ScanFilter = ScanFilter.ORIGINAL,
    val rotationDegrees: Float = 0f
)
