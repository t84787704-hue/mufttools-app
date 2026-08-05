package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.example.ui.components.CameraPreview
import com.example.ui.components.CropOverlay
import com.example.util.CropCorners

@Composable
fun PdfScannerScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    var cropCorners by remember {
        mutableStateOf(
            CropCorners(
                topLeft = Offset(0.15f, 0.15f),
                topRight = Offset(0.85f, 0.15f),
                bottomRight = Offset(0.85f, 0.85f),
                bottomLeft = Offset(0.15f, 0.85f)
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize()
        )

        CropOverlay(
            cropCorners = cropCorners,
            onCornersChanged = { cropCorners = it },
            modifier = Modifier.fillMaxSize()
        )
    }
}