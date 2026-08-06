package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanPrimary
import com.example.util.CropCorners

private fun Float.coerceInSafe(minVal: Float, maxVal: Float): Float {
    val actualMin = Math.min(minVal, maxVal)
    val actualMax = Math.max(minVal, maxVal)
    return this.coerceIn(actualMin, actualMax)
}

@Composable
fun CropOverlay(
    cropCorners: CropCorners,
    onCornersChanged: (CropCorners) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeHandle by remember { mutableStateOf<String?>(null) }
    val currentCropCorners by rememberUpdatedState(cropCorners)
    val currentOnCornersChanged by rememberUpdatedState(onCornersChanged)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val corners = currentCropCorners

                            val tl = Offset(corners.topLeft.x * width, corners.topLeft.y * height)
                            val tr = Offset(corners.topRight.x * width, corners.topRight.y * height)
                            val br = Offset(corners.bottomRight.x * width, corners.bottomRight.y * height)
                            val bl = Offset(corners.bottomLeft.x * width, corners.bottomLeft.y * height)

                            val touchRadius = 60.dp.toPx()

                            activeHandle = when {
                                (offset - tl).getDistance() < touchRadius -> "TL"
                                (offset - tr).getDistance() < touchRadius -> "TR"
                                (offset - br).getDistance() < touchRadius -> "BR"
                                (offset - bl).getDistance() < touchRadius -> "BL"
                                else -> null
                            }
                        },
                        onDragEnd = { activeHandle = null },
                        onDragCancel = { activeHandle = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            if (width <= 0f || height <= 0f) return@detectDragGestures

                            val dx = dragAmount.x / width
                            val dy = dragAmount.y / height
                            val corners = currentCropCorners

                            val newCorners = when (activeHandle) {
                                "TL" -> corners.copy(
                                    topLeft = Offset(
                                        (corners.topLeft.x + dx).coerceInSafe(0f, corners.topRight.x - 0.05f),
                                        (corners.topLeft.y + dy).coerceInSafe(0f, corners.bottomLeft.y - 0.05f)
                                    )
                                )
                                "TR" -> corners.copy(
                                    topRight = Offset(
                                        (corners.topRight.x + dx).coerceInSafe(corners.topLeft.x + 0.05f, 1f),
                                        (corners.topRight.y + dy).coerceInSafe(0f, corners.bottomRight.y - 0.05f)
                                    )
                                )
                                "BR" -> corners.copy(
                                    bottomRight = Offset(
                                        (corners.bottomRight.x + dx).coerceInSafe(corners.bottomLeft.x + 0.05f, 1f),
                                        (corners.bottomRight.y + dy).coerceInSafe(corners.topRight.y + 0.05f, 1f)
                                    )
                                )
                                "BL" -> corners.copy(
                                    bottomLeft = Offset(
                                        (corners.bottomLeft.x + dx).coerceInSafe(0f, corners.bottomRight.x - 0.05f),
                                        (corners.bottomLeft.y + dy).coerceInSafe(corners.topLeft.y + 0.05f, 1f)
                                    )
                                )
                                else -> corners
                            }

                            currentOnCornersChanged(newCorners)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val corners = currentCropCorners
            val tl = Offset(corners.topLeft.x * width, corners.topLeft.y * height)
            val tr = Offset(corners.topRight.x * width, corners.topRight.y * height)
            val br = Offset(corners.bottomRight.x * width, corners.bottomRight.y * height)
            val bl = Offset(corners.bottomLeft.x * width, corners.bottomLeft.y * height)

            val fullPath = Path().apply {
                addRect(Rect(0f, 0f, width, height))
            }
            val cropPath = Path().apply {
                moveTo(tl.x, tl.y)
                lineTo(tr.x, tr.y)
                lineTo(br.x, br.y)
                lineTo(bl.x, bl.y)
                close()
            }

            val overlayPath = Path.combine(
                PathOperation.Difference,
                fullPath,
                cropPath
            )

            drawPath(overlayPath, Color.Black.copy(alpha = 0.5f))

            drawPath(
                path = cropPath,
                color = CyanPrimary,
                style = Stroke(width = 3.dp.toPx())
            )

            val handleRadius = 14.dp.toPx()
            val points = listOf("TL" to tl, "TR" to tr, "BR" to br, "BL" to bl)

            points.forEach { (name, point) ->
                val isActive = activeHandle == name
                val currentRadius = if (isActive) handleRadius * 1.3f else handleRadius

                drawCircle(
                    color = if (isActive) Color.White else CyanPrimary,
                    radius = currentRadius,
                    center = point
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.4f),
                    radius = currentRadius + 2.dp.toPx(),
                    center = point,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}

