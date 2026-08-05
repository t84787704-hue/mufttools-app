package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT }

@Composable
fun CropOverlay(
    cropCorners: CropCorners,
    onCornersChanged: (CropCorners) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val cyan = remember { Color(0xFF00E5FF) }
    val dimColor = remember { Color.Black.copy(alpha = 0.62f) }
    val touchRadiusPx = with(density) { 44.dp.toPx() }
    val outerRadiusPx = with(density) { 14.dp.toPx() }
    val innerRadiusPx = with(density) { 6.5.dp.toPx() }
    val bracketLenPx = with(density) { 28.dp.toPx() }
    val bracketThickPx = with(density) { 4.dp.toPx() }
    val bracketThickWhitePx = with(density) { 6.5.dp.toPx() }
    val minSep = 0.12f

    var draggingCorner by remember { mutableStateOf<Corner?>(null) }
    val currentCorners = rememberUpdatedState(cropCorners)
    val callback = rememberUpdatedState(onCornersChanged)

    fun constrain(new: Offset, type: Corner, existing: CropCorners): Offset {
        var x = new.x.coerceIn(0f, 1f)
        var y = new.y.coerceIn(0f, 1f)
        when (type) {
            Corner.TOP_LEFT -> {
                x = x.coerceIn(0f, minOf(existing.topRight.x, existing.bottomRight.x) - minSep)
                y = y.coerceIn(0f, minOf(existing.bottomLeft.y, existing.bottomRight.y) - minSep)
            }
            Corner.TOP_RIGHT -> {
                x = x.coerceIn(maxOf(existing.topLeft.x, existing.bottomLeft.x) + minSep, 1f)
                y = y.coerceIn(0f, minOf(existing.bottomLeft.y, existing.bottomRight.y) - minSep)
            }
            Corner.BOTTOM_RIGHT -> {
                x = x.coerceIn(maxOf(existing.bottomLeft.x, existing.topLeft.x) + minSep, 1f)
                y = y.coerceIn(maxOf(existing.topLeft.y, existing.topRight.y) + minSep, 1f)
            }
            Corner.BOTTOM_LEFT -> {
                x = x.coerceIn(0f, minOf(existing.bottomRight.x, existing.topRight.x) - minSep)
                y = y.coerceIn(maxOf(existing.topLeft.y, existing.topRight.y) + minSep, 1f)
            }
        }
        return Offset(x, y)
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { pos ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val c = currentCorners.value
                    val tl = Offset(c.topLeft.x * w, c.topLeft.y * h)
                    val tr = Offset(c.topRight.x * w, c.topRight.y * h)
                    val br = Offset(c.bottomRight.x * w, c.bottomRight.y * h)
                    val bl = Offset(c.bottomLeft.x * w, c.bottomLeft.y * h)
                    val dTL = (pos - tl).getDistance()
                    val dTR = (pos - tr).getDistance()
                    val dBR = (pos - br).getDistance()
                    val dBL = (pos - bl).getDistance()
                    val min = minOf(dTL, dTR, dBR, dBL)
                    if (min > touchRadiusPx) return@detectDragGestures
                    draggingCorner = when (min) {
                        dTL -> Corner.TOP_LEFT
                        dTR -> Corner.TOP_RIGHT
                        dBR -> Corner.BOTTOM_RIGHT
                        else -> Corner.BOTTOM_LEFT
                    }
                },
                onDragEnd = { draggingCorner = null },
                onDragCancel = { draggingCorner = null },
                onDrag = { change, _ ->
                    change.consume()
                    val type = draggingCorner ?: return@detectDragGestures
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val norm = Offset((change.position.x / w).coerceIn(0f, 1f), (change.position.y / h).coerceIn(0f, 1f))
                    val constrained = constrain(norm, type, currentCorners.value)
                    val newCorners = when (type) {
                        Corner.TOP_LEFT -> currentCorners.value.copy(topLeft = constrained)
                        Corner.TOP_RIGHT -> currentCorners.value.copy(topRight = constrained)
                        Corner.BOTTOM_RIGHT -> currentCorners.value.copy(bottomRight = constrained)
                        Corner.BOTTOM_LEFT -> currentCorners.value.copy(bottomLeft = constrained)
                    }
                    callback.value(newCorners)
                }
            )
        }
    ) {
        val w = size.width
        val h = size.height
        val tl = Offset(cropCorners.topLeft.x * w, cropCorners.topLeft.y * h)
        val tr = Offset(cropCorners.topRight.x * w, cropCorners.topRight.y * h)
        val br = Offset(cropCorners.bottomRight.x * w, cropCorners.bottomRight.y * h)
        val bl = Offset(cropCorners.bottomLeft.x * w, cropCorners.bottomLeft.y * h)

        val dimPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, Size(w, h)))
            moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bl.x, bl.y); close()
        }
        drawPath(dimPath, dimColor)

        val borderPath = Path().apply {
            moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(bl.x, bl.y); close()
        }
        drawPath(borderPath, Color.White.copy(alpha = 0.9f), style = Stroke(bracketThickWhitePx, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(borderPath, cyan, style = Stroke(bracketThickPx, cap = StrokeCap.Round, join = StrokeJoin.Round))

        fun drawHandle(p: Offset, type: Corner) {
            val hx = if (type == Corner.TOP_LEFT || type == Corner.BOTTOM_LEFT) 1f else -1f
            val vy = if (type == Corner.TOP_LEFT || type == Corner.TOP_RIGHT) 1f else -1f
            val hEnd = Offset(p.x + hx * bracketLenPx, p.y)
            val vEnd = Offset(p.x, p.y + vy * bracketLenPx)
            drawLine(Color.White, p, hEnd, bracketThickWhitePx, StrokeCap.Round)
            drawLine(Color.White, p, vEnd, bracketThickWhitePx, StrokeCap.Round)
            drawLine(cyan, p, hEnd, bracketThickPx, StrokeCap.Round)
            drawLine(cyan, p, vEnd, bracketThickPx, StrokeCap.Round)
            drawCircle(Color.Black.copy(alpha = 0.25f), outerRadiusPx + 5.dp.toPx(), p)
            drawCircle(Color.White, outerRadiusPx, p)
            drawCircle(cyan, innerRadiusPx, p)
        }
        drawHandle(tl, Corner.TOP_LEFT)
        drawHandle(tr, Corner.TOP_RIGHT)
        drawHandle(br, Corner.BOTTOM_RIGHT)
        drawHandle(bl, Corner.BOTTOM_LEFT)
    }
}