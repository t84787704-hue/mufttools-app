package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CropOverlay(
    modifier: Modifier = Modifier,
    cropRect: Rect? = null,
    onCropRectChanged: ((Rect) -> Unit)? = null,
    points: List<Offset>? = null,
    onPointsChanged: ((List<Offset>) -> Unit)? = null,
    overlayColor: Color = Color.Black.copy(alpha = 0.55f),
    borderColor: Color = Color.White,
    borderWidth: Dp = 2.dp,
    gridColor: Color = Color.White.copy(alpha = 0.4f)
) {
    val density = LocalDensity.current
    val borderPx = with(density) { borderWidth.toPx() }

    var internalRect by remember { mutableStateOf(cropRect ?: Rect.Zero) }
    var internalPoints by remember { mutableStateOf(points) }

    // Update from outside
    LaunchedEffect(cropRect) { if (cropRect != null) internalRect = cropRect }
    LaunchedEffect(points) { if (points != null) internalPoints = points }

    Canvas(modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                // Simple move logic for Rect mode
                if (internalRect != Rect.Zero) {
                    val newRect = internalRect.translate(dragAmount)
                    internalRect = newRect
                    onCropRectChanged?.invoke(newRect)
                }
            }
        }
    ) {
        val activeRect = internalRect
        val rectToDraw = if (activeRect == Rect.Zero) {
            // first time init - center 80%
            val pad = size.width * 0.1f
            Rect(Offset(pad, pad), Size(size.width - pad * 2, size.height - pad * 2))
        } else activeRect

        if (internalRect == Rect.Zero) {
            internalRect = rectToDraw
        }

        // 1. Dim outside area
        drawRect(color = overlayColor, size = Size(size.width, rectToDraw.top))
        drawRect(color = overlayColor, topLeft = Offset(0f, rectToDraw.top), size = Size(rectToDraw.left, rectToDraw.height))
        drawRect(color = overlayColor, topLeft = Offset(rectToDraw.right, rectToDraw.top), size = Size(size.width - rectToDraw.right, rectToDraw.height))
        drawRect(color = overlayColor, topLeft = Offset(0f, rectToDraw.bottom), size = Size(size.width, size.height - rectToDraw.bottom))

        // 2. Border
        drawRect(
            color = borderColor,
            topLeft = rectToDraw.topLeft,
            size = rectToDraw.size,
            style = Stroke(width = borderPx)
        )

        // 3. Grid - rule of thirds
        val thirdW = rectToDraw.width / 3
        val thirdH = rectToDraw.height / 3
        for (i in 1..2) {
            drawLine(gridColor, Offset(rectToDraw.left + thirdW * i, rectToDraw.top), Offset(rectToDraw.left + thirdW * i, rectToDraw.bottom), strokeWidth = 1f)
            drawLine(gridColor, Offset(rectToDraw.left, rectToDraw.top + thirdH * i), Offset(rectToDraw.right, rectToDraw.top + thirdH * i), strokeWidth = 1f)
        }

        // 4. Corner handles
        val handleSize = 16.dp.toPx()
        listOf(rectToDraw.topLeft, rectToDraw.topRight, rectToDraw.bottomLeft, rectToDraw.bottomRight).forEach {
            drawRect(color = borderColor, topLeft = Offset(it.x - handleSize / 2, it.y - handleSize / 2), size = Size(handleSize, handleSize))
        }
    }
}