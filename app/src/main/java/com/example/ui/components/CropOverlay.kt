package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.util.CropCorners

private enum class Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT
}

@Composable
fun CropOverlay(
    cropCorners: CropCorners,
    onCornersChanged: (CropCorners) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeCorner by remember { mutableStateOf<Corner?>(null) }
    val minGap = 0.05f

    Canvas(
        modifier = modifier.pointerInput(cropCorners) {
            val width = size.width.toFloat()
            val height = size.height.toFloat()

            if (width <= 0f || height <= 0f) return@pointerInput

            val tlPx = Offset(cropCorners.topLeft.x * width, cropCorners.topLeft.y * height)
            val trPx = Offset(cropCorners.topRight.x * width, cropCorners.topRight.y * height)
            val brPx = Offset(cropCorners.bottomRight.x * width, cropCorners.bottomRight.y * height)
            val blPx = Offset(cropCorners.bottomLeft.x * width, cropCorners.bottomLeft.y * height)

            detectDragGestures(
                onDragStart = { touchOffset ->
                    val distTl = (touchOffset - tlPx).getDistance()
                    val distTr = (touchOffset - trPx).getDistance()
                    val distBr = (touchOffset - brPx).getDistance()
                    val distBl = (touchOffset - blPx).getDistance()

                    val touchRadiusPx = 60f
                    val minDist = listOf(distTl, distTr, distBr, distBl).minOrNull() ?: Float.MAX_VALUE

                    activeCorner = if (minDist <= touchRadiusPx) {
                        when (minDist) {
                            distTl -> Corner.TOP_LEFT
                            distTr -> Corner.TOP_RIGHT
                            distBr -> Corner.BOTTOM_RIGHT
                            else -> Corner.BOTTOM_LEFT
                        }
                    } else {
                        null
                    }
                },
                onDrag = { change, dragAmount ->
                    val corner = activeCorner ?: return@detectDragGestures
                    change.consume()

                    val deltaX = dragAmount.x / width
                    val deltaY = dragAmount.y / height

                    val updatedCorners = when (corner) {
                        Corner.TOP_LEFT -> {
                            val newX = (cropCorners.topLeft.x + deltaX).coerceIn(0f, cropCorners.topRight.x - minGap)
                            val newY = (cropCorners.topLeft.y + deltaY).coerceIn(0f, cropCorners.bottomLeft.y - minGap)
                            cropCorners.copy(topLeft = Offset(newX, newY))
                        }
                        Corner.TOP_RIGHT -> {
                            val newX = (cropCorners.topRight.x + deltaX).coerceIn(cropCorners.topLeft.x + minGap, 1f)
                            val newY = (cropCorners.topRight.y + deltaY).coerceIn(0f, cropCorners.bottomRight.y - minGap)
                            cropCorners.copy(topRight = Offset(newX, newY))
                        }
                        Corner.BOTTOM_RIGHT -> {
                            val newX = (cropCorners.bottomRight.x + deltaX).coerceIn(cropCorners.bottomLeft.x + minGap, 1f)
                            val newY = (cropCorners.bottomRight.y + deltaY).coerceIn(cropCorners.topRight.y + minGap, 1f)
                            cropCorners.copy(bottomRight = Offset(newX, newY))
                        }
                        Corner.BOTTOM_LEFT -> {
                            val newX = (cropCorners.bottomLeft.x + deltaX).coerceIn(0f, cropCorners.bottomRight.x - minGap)
                            val newY = (cropCorners.bottomLeft.y + deltaY).coerceIn(cropCorners.topLeft.y + minGap, 1f)
                            cropCorners.copy(bottomLeft = Offset(newX, newY))
                        }
                    }
                    onCornersChanged(updatedCorners)
                },
                onDragEnd = { activeCorner = null },
                onDragCancel = { activeCorner = null }
            )
        }
    ) {
        val width = size.width
        val height = size.height

        val tl = Offset(cropCorners.topLeft.x * width, cropCorners.topLeft.y * height)
        val tr = Offset(cropCorners.topRight.x * width, cropCorners.topRight.y * height)
        val br = Offset(cropCorners.bottomRight.x * width, cropCorners.bottomRight.y * height)
        val bl = Offset(cropCorners.bottomLeft.x * width, cropCorners.bottomLeft.y * height)

        val overlayPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, width, height))
            moveTo(tl.x, tl.y)
            lineTo(tr.x, tr.y)
            lineTo(br.x, br.y)
            lineTo(bl.x, bl.y)
            close()
        }
        drawPath(
            path = overlayPath,
            color = Color.Black.copy(alpha = 0.5f)
        )

        val borderPath = Path().apply {
            moveTo(tl.x, tl.y)
            lineTo(tr.x, tr.y)
            lineTo(br.x, br.y)
            lineTo(bl.x, bl.y)
            close()
        }
        drawPath(
            path = borderPath,
            color = Color.Cyan,
            style = Stroke(width = 2.dp.toPx())
        )

        val radius = 12.dp.toPx()
        listOf(tl, tr, br, bl).forEach { center ->
            drawCircle(
                color = Color.Cyan,
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = radius * 0.4f,
                center = center
            )
        }
    }
}
