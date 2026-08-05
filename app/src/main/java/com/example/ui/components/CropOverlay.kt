package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanPrimary
import com.example.util.CropCorners

@Composable
fun CropOverlay(
    cropCorners: CropCorners,
    onCornersChanged: (CropCorners) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeHandle by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(cropCorners) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            val tl = Offset(cropCorners.topLeft.x * width, cropCorners.topLeft.y * height)
                            val tr = Offset(cropCorners.topRight.x * width, cropCorners.topRight.y * height)
                            val br = Offset(cropCorners.bottomRight.x * width, cropCorners.bottomRight.y * height)
                            val bl = Offset(cropCorners.bottomLeft.x * width, cropCorners.bottomLeft.y * height)

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

                            val newCorners = when (activeHandle) {
                                "TL" -> cropCorners.copy(
                                    topLeft = Offset(
                                        (cropCorners.topLeft.x + dx).coerceIn(0f, cropCorners.topRight.x - 0.05f),
                                        (cropCorners.topLeft.y + dy).coerceIn(0f, cropCorners.bottomLeft.y - 0.05f)
                                    )
                                )
                                "TR" -> cropCorners.copy(
                                    topRight = Offset(
                                        (cropCorners.topRight.x + dx).coerceIn(cropCorners.topLeft.x + 0.05f, 1f),
                                        (cropCorners.topRight.y + dy).coerceIn(0f, cropCorners.bottomRight.y - 0.05f)
                                    )
                                )
                                "BR" -> cropCorners.copy(
                                    bottomRight = Offset(
                                        (cropCorners.bottomRight.x + dx).coerceIn(cropCorners.bottomLeft.x + 0.05f, 1f),
                                        (cropCorners.bottomRight.y + dy).coerceIn(cropCorners.topRight.y + 0.05f, 1f)
                                    )
                                )
                                "BL" -> cropCorners.copy(
                                    bottomLeft = Offset(
                                        (cropCorners.bottomLeft.x + dx).coerceIn(0f, cropCorners.bottomRight.x - 0.05f),
                                        (cropCorners.bottomLeft.y + dy).coerceIn(cropCorners.topLeft.y + 0.05f, 1f)
                                    )
                                )
                                else -> cropCorners
                            }

                            onCornersChanged(newCorners)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val tl = Offset(cropCorners.topLeft.x * width, cropCorners.topLeft.y * height)
            val tr = Offset(cropCorners.topRight.x * width, cropCorners.topRight.y * height)
            val br = Offset(cropCorners.bottomRight.x * width, cropCorners.bottomRight.y * height)
            val bl = Offset(cropCorners.bottomLeft.x * width, cropCorners.bottomLeft.y * height)

            // Outer dimmed overlay path
            val outerPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            val innerPath = Path().apply {
                moveTo(tl.x, tl.y)
                lineTo(tr.x, tr.y)
                lineTo(br.x, br.y)
                lineTo(bl.x, bl.y)
                close()
            }

            drawPath(outerPath, Color.Black.copy(alpha = 0.45f))
            drawPath(innerPath, Color.Transparent)

            // Quad bounding box crop outline
            drawPath(
                path = innerPath,
                color = CyanPrimary,
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw Corner handles
            val handleRadius = 14.dp.toPx()
            val handleColor = CyanPrimary
            val activeColor = Color.White

            val points = listOf(
                "TL" to tl,
                "TR" to tr,
                "BR" to br,
                "BL" to bl
            )

            points.forEach { (name, point) ->
                val isActive = activeHandle == name
                val currentRadius = if (isActive) handleRadius * 1.3f else handleRadius

                drawCircle(
                    color = if (isActive) activeColor else handleColor,
                    radius = currentRadius,
                    center = point
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = currentRadius + 2.dp.toPx(),
                    center = point,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}
