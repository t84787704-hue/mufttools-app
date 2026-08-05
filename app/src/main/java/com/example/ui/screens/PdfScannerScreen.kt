package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.ui.components.CameraPreview
import com.example.ui.components.CropCorners
import com.example.ui.components.CropOverlay

@Composable
fun PdfScannerScreen() {
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

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(modifier = Modifier.fillMaxSize())
        
        CropOverlay(
            cropCorners = cropCorners,
            onCornersChanged = { cropCorners = it },
            modifier = Modifier.fillMaxSize()
        )
    }
}