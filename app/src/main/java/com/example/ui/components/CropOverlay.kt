package com.freetools.offline.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

enum class CropHandle {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER, NONE
}

@Composable
fun CropOverlay(
    modifier: Modifier = Modifier,
    aspectRatio: Float? = null, // null = free crop, 1f = square, 16/9f etc
    overlayColor: Color = Color.Black.copy(alpha = 0.6f),
    borderColor: Color = Color.White,
    onCropRectChanged: (Rect) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()

        var cropRect by remember {
            mutableStateOf(
                Rect(
                    offset = Offset(maxWidth * 0.15f, maxHeight * 0.2f),
                    size = Size(maxWidth * 0.7f, maxHeight * 0.5f)
                )
            )
        }
        var currentHandle by remember { mutableStateOf(CropHandle.NONE) }
        val handleRadius = with(density) { 20.dp.toPx() }
        val minSize = with(density) { 80.dp.toPx() }

        fun getHandleAt(offset: Offset): CropHandle {
            return when {
                abs(offset.x - cropRect.left) < handleRadius && abs(offset.y - cropRect.top) < handleRadius -> CropHandle.TOP_LEFT
                abs(offset.x - cropRect.right) < handleRadius && abs(offset.y - cropRect.top) < handleRadius -> CropHandle.TOP_RIGHT
                abs(offset.x - cropRect.left) < handleRadius && abs(offset.y - cropRect.bottom) < handleRadius -> CropHandle.BOTTOM_LEFT
                abs(offset.x - cropRect.right) < handleRadius && abs(offset.y - cropRect.bottom) < handleRadius -> CropHandle.BOTTOM_RIGHT
                cropRect.contains(offset) -> CropHandle.CENTER
                else -> CropHandle.NONE
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentHandle = getHandleAt(offset)
                        },
                        onDragEnd = {
                            currentHandle = CropHandle.NONE
                            onCropRectChanged(cropRect)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            var newRect = cropRect

                            when (currentHandle) {
                                CropHandle.CENTER -> {
                                    newRect = newRect.translate(dragAmount)
                                }
                                CropHandle.TOP_LEFT -> {
                                    newRect = Rect(
                                        left = (newRect.left + dragAmount.x).coerceIn(0f, newRect.right - minSize),
                                        top = (newRect.top + dragAmount.y).coerceIn(0f, newRect.bottom - minSize),
                                        right = newRect.right,
                                        bottom = newRect.bottom
                                    )
                                }
                                CropHandle.TOP_RIGHT -> {
                                    newRect = Rect(
                                        left = newRect.left,
                                        top = (newRect.top + dragAmount.y).coerceIn(0f, newRect.bottom - minSize),
                                        right = (newRect.right + dragAmount.x).coerceIn(newRect.left + minSize, maxWidth),
                                        bottom = newRect.bottom
                                    )
                                }
                                CropHandle.BOTTOM_LEFT -> {
                                    newRect = Rect(
                                        left = (newRect.left + dragAmount.x).coerceIn(0f, newRect.right - minSize),
                                        top = newRect.top,
                                        right = newRect.right,
                                        bottom = (newRect.bottom + dragAmount.y).coerceIn(newRect.top + minSize, maxHeight)
                                    )
                                }
                                CropHandle.BOTTOM_RIGHT -> {
                                    newRect = Rect(
                                        left = newRect.left,
                                        top = newRect.top,
                                        right = (newRect.right + dragAmount.x).coerceIn(newRect.left + minSize, maxWidth),
                                        bottom = (newRect.bottom + dragAmount.y).coerceIn(newRect.top + minSize, maxHeight)
                                    )
                                }
                                else -> {}
                            }

                            // Aspect ratio lock
                            if (aspectRatio != null) {
                                val width = newRect.width
                                val height = width / aspectRatio
                                if (currentHandle == CropHandle.BOTTOM_RIGHT || currentHandle == CropHandle.TOP_RIGHT) {
                                    newRect = Rect(newRect.left, newRect.top, newRect.right, newRect.top + height)
                                }
                            }

                            // Keep inside bounds
                            if (newRect.left >= 0 && newRect.top >= 0 && newRect.right <= maxWidth && newRect.bottom <= maxHeight) {
                                cropRect = newRect
                            }
                        }
                    )
                }
        ) {
            // Dark overlay with hole
            val path = Path().apply {
                addRect(Rect(Offset.Zero, Size(maxWidth, maxHeight)))
                addRect(cropRect)
            }

            drawPath(
                path = path,
                color = overlayColor,
                // Use EvenOdd to create hole
            )

            // Actually draw overlay using clip
            drawRect(color = overlayColor, topLeft = Offset.Zero, size = Size(maxWidth, cropRect.top))
            drawRect(color = overlayColor, topLeft = Offset(0f, cropRect.top), size = Size(cropRect.left, cropRect.height))
            drawRect(color = overlayColor, topLeft = Offset(cropRect.right, cropRect.top), size = Size(maxWidth - cropRect.right, cropRect.height))
            drawRect(color = overlayColor, topLeft = Offset(0f, cropRect.bottom), size = Size(maxWidth, maxHeight - cropRect.bottom))

            // Border
            drawRect(
                color = borderColor,
                topLeft = cropRect.topLeft,
                size = cropRect.size,
                style = Stroke(width = 2.dp.toPx())
            )

            // Grid lines - Rule of thirds
            val thirdW = cropRect.width / 3
            val thirdH = cropRect.height / 3
            drawLine(borderColor.copy(alpha = 0.6f), Offset(cropRect.left + thirdW, cropRect.top), Offset(cropRect.left + thirdW, cropRect.bottom), strokeWidth = 1.dp.toPx())
            drawLine(borderColor.copy(alpha = 0.6f), Offset(cropRect.left + thirdW * 2, cropRect.top), Offset(cropRect.left + thirdW * 2, cropRect.bottom), strokeWidth = 1.dp.toPx())
            drawLine(borderColor.copy(alpha = 0.6f), Offset(cropRect.left, cropRect.top + thirdH), Offset(cropRect.right, cropRect.top + thirdH), strokeWidth = 1.dp.toPx())
            drawLine(borderColor.copy(alpha = 0.6f), Offset(cropRect.left, cropRect.top + thirdH * 2), Offset(cropRect.right, cropRect.top + thirdH * 2), strokeWidth = 1.dp.toPx())

            // Corner handles
            val handleSize = 14.dp.toPx()
            val handles = listOf(
                cropRect.topLeft,
                Offset(cropRect.right, cropRect.top),
                Offset(cropRect.left, cropRect.bottom),
                cropRect.bottomRight
            )
            handles.forEach {
                drawRect(
                    color = borderColor,
                    topLeft = Offset(it.x - handleSize / 2, it.y - handleSize / 2),
                    size = Size(handleSize, handleSize),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawRect(
                    color = borderColor,
                    topLeft = Offset(it.x - handleSize / 2, it.y - handleSize / 2),
                    size = Size(handleSize, handleSize)
                )
            }
        }
    }
}