package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

// Design Colors matching reference image (Image Tools 5-in-1 Purple Theme)
private val PurplePrimary = Color(0xFF9333EA)    // Vibrant Purple
private val PurpleGlow = Color(0xFFA855F7)       // Accent Purple
private val PurpleDarkBg = Color(0xFF0D0B18)     // Midnight Deep Dark
private val SurfaceCardBg = Color(0xFF171329)    // Rich Dark Surface
private val SurfaceCardActive = Color(0xFF231A3D) // Selected Active Surface
private val CardBorderColor = Color(0xFF2D234A)  // Subtle Card Border
private val ActiveBorderColor = Color(0xFFA855F7)// Active Highlight Border
private val DashedBorderColor = Color(0xFF3F3360)// Dashed Placeholder Border
private val GreenSavedText = Color(0xFF10B981)   // Emerald Green Size Savings
private val CrownGold = Color(0xFFF59E0B)        // Gold Crown Accent

enum class CropHandle {
    NONE, CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP, BOTTOM, LEFT, RIGHT
}

enum class CropRatio(val displayName: String) {
    FREE("Free"),
    ORIGINAL("Original"),
    SQUARE("1:1"),
    RATIO_4_3("4:3"),
    RATIO_16_9("16:9"),
    RATIO_3_4("3:4"),
    RATIO_9_16("9:16");

    fun getTargetRatio(bitmapWidth: Int, bitmapHeight: Int): Float? {
        return when (this) {
            FREE -> null
            ORIGINAL -> if (bitmapHeight > 0) bitmapWidth.toFloat() / bitmapHeight.toFloat() else 1f
            SQUARE -> 1.0f
            RATIO_4_3 -> 4f / 3f
            RATIO_16_9 -> 16f / 9f
            RATIO_3_4 -> 3f / 4f
            RATIO_9_16 -> 9f / 16f
        }
    }
}

private fun Float.coerceInSafe(minVal: Float, maxVal: Float): Float {
    val actualMin = Math.min(minVal, maxVal)
    val actualMax = Math.max(minVal, maxVal)
    return this.coerceIn(actualMin, actualMax)
}

fun calculateDefaultCropRect(imageRect: Rect, targetRatio: Float?): Rect {
    if (imageRect.width <= 0 || imageRect.height <= 0) return Rect.Zero
    if (targetRatio == null) {
        // Free ratio: default to 95% centered frame so handles are easy to grab
        val insetX = imageRect.width * 0.025f
        val insetY = imageRect.height * 0.025f
        return Rect(
            imageRect.left + insetX,
            imageRect.top + insetY,
            imageRect.right - insetX,
            imageRect.bottom - insetY
        )
    }

    val imgW = imageRect.width
    val imgH = imageRect.height
    val currentRatio = imgW / imgH

    return if (currentRatio > targetRatio) {
        val cropW = imgH * targetRatio
        val left = imageRect.left + (imgW - cropW) / 2f
        Rect(left, imageRect.top, left + cropW, imageRect.bottom)
    } else {
        val cropH = imgW / targetRatio
        val top = imageRect.top + (imgH - cropH) / 2f
        Rect(imageRect.left, top, imageRect.right, top + cropH)
    }
}

fun hitTestHandle(touch: Offset, cropRect: Rect, thresholdPx: Float = 80f): CropHandle {
    val l = cropRect.left
    val t = cropRect.top
    val r = cropRect.right
    val b = cropRect.bottom

    val distTopLeft = hypot(touch.x - l, touch.y - t)
    val distTopRight = hypot(touch.x - r, touch.y - t)
    val distBottomLeft = hypot(touch.x - l, touch.y - b)
    val distBottomRight = hypot(touch.x - r, touch.y - b)

    if (distTopLeft <= thresholdPx) return CropHandle.TOP_LEFT
    if (distTopRight <= thresholdPx) return CropHandle.TOP_RIGHT
    if (distBottomLeft <= thresholdPx) return CropHandle.BOTTOM_LEFT
    if (distBottomRight <= thresholdPx) return CropHandle.BOTTOM_RIGHT

    val distLeft = abs(touch.x - l)
    val distRight = abs(touch.x - r)
    val distTop = abs(touch.y - t)
    val distBottom = abs(touch.y - b)

    val withinY = touch.y in (t - thresholdPx)..(b + thresholdPx)
    val withinX = touch.x in (l - thresholdPx)..(r + thresholdPx)

    if (distLeft <= thresholdPx && withinY) return CropHandle.LEFT
    if (distRight <= thresholdPx && withinY) return CropHandle.RIGHT
    if (distTop <= thresholdPx && withinX) return CropHandle.TOP
    if (distBottom <= thresholdPx && withinX) return CropHandle.BOTTOM

    if (cropRect.contains(touch)) return CropHandle.CENTER

    return CropHandle.NONE
}

fun updateCropRect(
    current: Rect,
    imageRect: Rect,
    handle: CropHandle,
    dx: Float,
    dy: Float,
    targetRatio: Float?,
    minSize: Float = 40f
): Rect {
    var l = current.left
    var t = current.top
    var r = current.right
    var b = current.bottom

    if (handle == CropHandle.CENTER) {
        val w = current.width.coerceAtMost(imageRect.width)
        val h = current.height.coerceAtMost(imageRect.height)
        val maxL = (imageRect.right - w).coerceAtLeast(imageRect.left)
        val maxT = (imageRect.bottom - h).coerceAtLeast(imageRect.top)
        val newL = (l + dx).coerceInSafe(imageRect.left, maxL)
        val newT = (t + dy).coerceInSafe(imageRect.top, maxT)
        return Rect(newL, newT, newL + w, newT + h)
    }

    val maxRightForL = (r - minSize).coerceAtLeast(imageRect.left)
    val maxBottomForT = (b - minSize).coerceAtLeast(imageRect.top)
    val minLeftForR = (l + minSize).coerceAtMost(imageRect.right)
    val minTopForB = (t + minSize).coerceAtMost(imageRect.bottom)

    when (handle) {
        CropHandle.TOP_LEFT -> {
            l = (l + dx).coerceInSafe(imageRect.left, maxRightForL)
            t = (t + dy).coerceInSafe(imageRect.top, maxBottomForT)
            if (targetRatio != null) {
                val newW = r - l
                val newH = newW / targetRatio
                val calcT = b - newH
                t = calcT.coerceInSafe(imageRect.top, maxBottomForT)
                l = r - (b - t) * targetRatio
            }
        }
        CropHandle.TOP_RIGHT -> {
            r = (r + dx).coerceInSafe(minLeftForR, imageRect.right)
            t = (t + dy).coerceInSafe(imageRect.top, maxBottomForT)
            if (targetRatio != null) {
                val newW = r - l
                val newH = newW / targetRatio
                val calcT = b - newH
                t = calcT.coerceInSafe(imageRect.top, maxBottomForT)
                r = l + (b - t) * targetRatio
            }
        }
        CropHandle.BOTTOM_LEFT -> {
            l = (l + dx).coerceInSafe(imageRect.left, maxRightForL)
            b = (b + dy).coerceInSafe(minTopForB, imageRect.bottom)
            if (targetRatio != null) {
                val newW = r - l
                val newH = newW / targetRatio
                val calcB = t + newH
                b = calcB.coerceInSafe(minTopForB, imageRect.bottom)
                l = r - (b - t) * targetRatio
            }
        }
        CropHandle.BOTTOM_RIGHT -> {
            r = (r + dx).coerceInSafe(minLeftForR, imageRect.right)
            b = (b + dy).coerceInSafe(minTopForB, imageRect.bottom)
            if (targetRatio != null) {
                val newW = r - l
                val newH = newW / targetRatio
                val calcB = t + newH
                b = calcB.coerceInSafe(minTopForB, imageRect.bottom)
                r = l + (b - t) * targetRatio
            }
        }
        CropHandle.LEFT -> {
            l = (l + dx).coerceInSafe(imageRect.left, maxRightForL)
            if (targetRatio != null) {
                val newW = r - l
                val newH = newW / targetRatio
                val centerY = (t + b) / 2f
                t = (centerY - newH / 2f).coerceInSafe(imageRect.top, imageRect.bottom - minSize)
                b = (t + newH).coerceInSafe(t + minSize, imageRect.bottom)
            }
        }
        CropHandle.RIGHT -> {
            r = (r + dx).coerceInSafe(minLeftForR, imageRect.right)
            if (targetRatio != null) {
                val newW = r - l
                val newH = newW / targetRatio
                val centerY = (t + b) / 2f
                t = (centerY - newH / 2f).coerceInSafe(imageRect.top, imageRect.bottom - minSize)
                b = (t + newH).coerceInSafe(t + minSize, imageRect.bottom)
            }
        }
        CropHandle.TOP -> {
            t = (t + dy).coerceInSafe(imageRect.top, maxBottomForT)
            if (targetRatio != null) {
                val newH = b - t
                val newW = newH * targetRatio
                val centerX = (l + r) / 2f
                l = (centerX - newW / 2f).coerceInSafe(imageRect.left, imageRect.right - minSize)
                r = (l + newW).coerceInSafe(l + minSize, imageRect.right)
            }
        }
        CropHandle.BOTTOM -> {
            b = (b + dy).coerceInSafe(minTopForB, imageRect.bottom)
            if (targetRatio != null) {
                val newH = b - t
                val newW = newH * targetRatio
                val centerX = (l + r) / 2f
                l = (centerX - newW / 2f).coerceInSafe(imageRect.left, imageRect.right - minSize)
                r = (l + newW).coerceInSafe(l + minSize, imageRect.right)
            }
        }
        else -> {}
    }

    val finalL = l.coerceInSafe(imageRect.left, (imageRect.right - minSize).coerceAtLeast(imageRect.left))
    val finalT = t.coerceInSafe(imageRect.top, (imageRect.bottom - minSize).coerceAtLeast(imageRect.top))
    val finalR = r.coerceInSafe((finalL + minSize).coerceAtMost(imageRect.right), imageRect.right)
    val finalB = b.coerceInSafe((finalT + minSize).coerceAtMost(imageRect.bottom), imageRect.bottom)

    return Rect(finalL, finalT, finalR, finalB)
}

@Composable
private fun InteractiveCropEditor(
    bitmap: Bitmap,
    cropRatio: CropRatio,
    onCropRectChanged: (cropRect: Rect, imageRect: Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var imageRect by remember { mutableStateOf(Rect.Zero) }
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var activeHandle by remember { mutableStateOf(CropHandle.NONE) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        LaunchedEffect(bitmap, containerWidth, containerHeight) {
            if (containerWidth > 0 && containerHeight > 0 && bitmap.width > 0 && bitmap.height > 0) {
                val scale = Math.min(containerWidth / bitmap.width.toFloat(), containerHeight / bitmap.height.toFloat())
                val displayedWidth = bitmap.width * scale
                val displayedHeight = bitmap.height * scale
                val left = (containerWidth - displayedWidth) / 2f
                val top = (containerHeight - displayedHeight) / 2f

                val newImgRect = Rect(left, top, left + displayedWidth, top + displayedHeight)
                imageRect = newImgRect
                val newCropRect = calculateDefaultCropRect(newImgRect, cropRatio.getTargetRatio(bitmap.width, bitmap.height))
                cropRect = newCropRect
                onCropRectChanged(newCropRect, newImgRect)
            }
        }

        LaunchedEffect(cropRatio) {
            if (imageRect.width > 0 && imageRect.height > 0) {
                val newCropRect = calculateDefaultCropRect(imageRect, cropRatio.getTargetRatio(bitmap.width, bitmap.height))
                cropRect = newCropRect
                onCropRectChanged(newCropRect, imageRect)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(cropRatio, imageRect) {
                    detectDragGestures(
                        onDragStart = { touch ->
                            activeHandle = hitTestHandle(touch, cropRect)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (activeHandle != CropHandle.NONE && imageRect.width > 0) {
                                val updated = updateCropRect(
                                    current = cropRect,
                                    imageRect = imageRect,
                                    handle = activeHandle,
                                    dx = dragAmount.x,
                                    dy = dragAmount.y,
                                    targetRatio = cropRatio.getTargetRatio(bitmap.width, bitmap.height)
                                )
                                cropRect = updated
                                onCropRectChanged(updated, imageRect)
                            }
                        },
                        onDragEnd = { activeHandle = CropHandle.NONE },
                        onDragCancel = { activeHandle = CropHandle.NONE }
                    )
                }
                .pointerInput(cropRatio, imageRect) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f && activeHandle == CropHandle.NONE && imageRect.width > 0) {
                            val center = cropRect.center
                            val maxW = imageRect.width
                            val maxH = imageRect.height
                            val newW = (cropRect.width * zoom).coerceInSafe(40f, maxW)
                            val ratio = cropRatio.getTargetRatio(bitmap.width, bitmap.height)
                            val newH = if (ratio != null) newW / ratio else (cropRect.height * zoom).coerceInSafe(40f, maxH)

                            val maxL = (imageRect.right - newW).coerceAtLeast(imageRect.left)
                            val maxT = (imageRect.bottom - newH).coerceAtLeast(imageRect.top)

                            val l = (center.x - newW / 2f).coerceInSafe(imageRect.left, maxL)
                            val t = (center.y - newH / 2f).coerceInSafe(imageRect.top, maxT)
                            val r = (l + newW).coerceAtMost(imageRect.right)
                            val b = (t + newH).coerceAtMost(imageRect.bottom)

                            val updated = Rect(l, t, r, b)
                            cropRect = updated
                            onCropRectChanged(updated, imageRect)
                        } else if (pan != Offset.Zero && activeHandle == CropHandle.CENTER) {
                            val updated = updateCropRect(
                                current = cropRect,
                                imageRect = imageRect,
                                handle = CropHandle.CENTER,
                                dx = pan.x,
                                dy = pan.y,
                                targetRatio = cropRatio.getTargetRatio(bitmap.width, bitmap.height)
                            )
                            cropRect = updated
                            onCropRectChanged(updated, imageRect)
                        }
                    }
                }
        ) {
            if (imageRect.width > 0 && imageRect.height > 0) {
                // 1. Draw Image
                drawImage(
                    image = bitmap.asImageBitmap(),
                    dstOffset = IntOffset(imageRect.left.roundToInt(), imageRect.top.roundToInt()),
                    dstSize = IntSize(imageRect.width.roundToInt(), imageRect.height.roundToInt())
                )

                // 2. Draw Dim Overlays outside cropRect
                val dimColor = Color.Black.copy(alpha = 0.62f)

                // Top
                if (cropRect.top > imageRect.top) {
                    drawRect(
                        color = dimColor,
                        topLeft = Offset(imageRect.left, imageRect.top),
                        size = Size(imageRect.width, cropRect.top - imageRect.top)
                    )
                }
                // Bottom
                if (imageRect.bottom > cropRect.bottom) {
                    drawRect(
                        color = dimColor,
                        topLeft = Offset(imageRect.left, cropRect.bottom),
                        size = Size(imageRect.width, imageRect.bottom - cropRect.bottom)
                    )
                }
                // Left
                if (cropRect.left > imageRect.left) {
                    drawRect(
                        color = dimColor,
                        topLeft = Offset(imageRect.left, cropRect.top),
                        size = Size(cropRect.left - imageRect.left, cropRect.height)
                    )
                }
                // Right
                if (imageRect.right > cropRect.right) {
                    drawRect(
                        color = dimColor,
                        topLeft = Offset(cropRect.right, cropRect.top),
                        size = Size(imageRect.right - cropRect.right, cropRect.height)
                    )
                }

                // 3. Draw Crop Frame Border
                drawRect(
                    color = Color.White,
                    topLeft = cropRect.topLeft,
                    size = cropRect.size,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 4. Draw Rule of Thirds Grid Lines
                val gridColor = Color.White.copy(alpha = 0.45f)
                val gridStrokeWidth = 1.dp.toPx()
                val thirdW = cropRect.width / 3f
                val thirdH = cropRect.height / 3f

                // Vertical grid lines
                drawLine(gridColor, Offset(cropRect.left + thirdW, cropRect.top), Offset(cropRect.left + thirdW, cropRect.bottom), gridStrokeWidth)
                drawLine(gridColor, Offset(cropRect.left + thirdW * 2f, cropRect.top), Offset(cropRect.left + thirdW * 2f, cropRect.bottom), gridStrokeWidth)

                // Horizontal grid lines
                drawLine(gridColor, Offset(cropRect.left, cropRect.top + thirdH), Offset(cropRect.right, cropRect.top + thirdH), gridStrokeWidth)
                drawLine(gridColor, Offset(cropRect.left, cropRect.top + thirdH * 2f), Offset(cropRect.right, cropRect.top + thirdH * 2f), gridStrokeWidth)

                // 5. Draw Corner Handles (Thick L-shapes)
                val cornerLen = 18.dp.toPx()
                val handleStroke = 3.5.dp.toPx()
                val handleColor = Color.White

                // Top-Left
                drawLine(handleColor, Offset(cropRect.left, cropRect.top), Offset(cropRect.left + cornerLen, cropRect.top), handleStroke)
                drawLine(handleColor, Offset(cropRect.left, cropRect.top), Offset(cropRect.left, cropRect.top + cornerLen), handleStroke)

                // Top-Right
                drawLine(handleColor, Offset(cropRect.right, cropRect.top), Offset(cropRect.right - cornerLen, cropRect.top), handleStroke)
                drawLine(handleColor, Offset(cropRect.right, cropRect.top), Offset(cropRect.right, cropRect.top + cornerLen), handleStroke)

                // Bottom-Left
                drawLine(handleColor, Offset(cropRect.left, cropRect.bottom), Offset(cropRect.left + cornerLen, cropRect.bottom), handleStroke)
                drawLine(handleColor, Offset(cropRect.left, cropRect.bottom), Offset(cropRect.left, cropRect.bottom - cornerLen), handleStroke)

                // Bottom-Right
                drawLine(handleColor, Offset(cropRect.right, cropRect.bottom), Offset(cropRect.right - cornerLen, cropRect.bottom), handleStroke)
                drawLine(handleColor, Offset(cropRect.right, cropRect.bottom), Offset(cropRect.right, cropRect.bottom - cornerLen), handleStroke)

                // 6. Draw Edge Handles (Center Bars)
                val edgeLen = 16.dp.toPx()

                // Top
                drawLine(handleColor, Offset(cropRect.center.x - edgeLen / 2f, cropRect.top), Offset(cropRect.center.x + edgeLen / 2f, cropRect.top), handleStroke)
                // Bottom
                drawLine(handleColor, Offset(cropRect.center.x - edgeLen / 2f, cropRect.bottom), Offset(cropRect.center.x + edgeLen / 2f, cropRect.bottom), handleStroke)
                // Left
                drawLine(handleColor, Offset(cropRect.left, cropRect.center.y - edgeLen / 2f), Offset(cropRect.left, cropRect.center.y + edgeLen / 2f), handleStroke)
                // Right
                drawLine(handleColor, Offset(cropRect.right, cropRect.center.y - edgeLen / 2f), Offset(cropRect.right, cropRect.center.y + edgeLen / 2f), handleStroke)
            }
        }
    }
}

enum class ToolMode {
    CROP, RESIZE, COMPRESS, CONVERT, ROTATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolsScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Image Source & Processing State
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalFileName by remember { mutableStateOf("") }
    var originalSizeBytes by remember { mutableStateOf(0L) }
    var originalFormat by remember { mutableStateOf("") }
    var savedUri by remember { mutableStateOf<Uri?>(null) }

    // Active Tool Mode (Default: Crop as shown in reference)
    var activeToolMode by remember { mutableStateOf(ToolMode.CROP) }

    // Compression Quality State
    var compressionQuality by remember { mutableFloatStateOf(80f) } // 80% High as shown in reference

    // Resize State
    var targetWidthStr by remember { mutableStateOf("1920") }
    var targetHeightStr by remember { mutableStateOf("1080") }
    var maintainAspect by remember { mutableStateOf(true) }

    // Undo & Redo History Stacks
    var undoStack by remember { mutableStateOf(listOf<Bitmap>()) }
    var redoStack by remember { mutableStateOf(listOf<Bitmap>()) }

    val pushToUndo: (Bitmap?) -> Unit = { currentBmp ->
        if (currentBmp != null) {
            val newStack = undoStack.toMutableList()
            newStack.add(currentBmp)
            if (newStack.size > 20) {
                newStack.removeAt(0)
            }
            undoStack = newStack
            redoStack = emptyList()
        }
    }

    val performUndo: () -> Unit = {
        if (undoStack.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val current = editedBitmap ?: originalBitmap
            if (current != null) {
                val newRedo = redoStack.toMutableList()
                newRedo.add(current)
                redoStack = newRedo
            }
            val previous = undoStack.last()
            undoStack = undoStack.dropLast(1)
            editedBitmap = previous
            targetWidthStr = previous.width.toString()
            targetHeightStr = previous.height.toString()
            scope.launch { snackbarHostState.showSnackbar("Undo applied") }
        }
    }

    val performRedo: () -> Unit = {
        if (redoStack.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val current = editedBitmap ?: originalBitmap
            if (current != null) {
                val newUndo = undoStack.toMutableList()
                newUndo.add(current)
                undoStack = newUndo
            }
            val next = redoStack.last()
            redoStack = redoStack.dropLast(1)
            editedBitmap = next
            targetWidthStr = next.width.toString()
            targetHeightStr = next.height.toString()
            scope.launch { snackbarHostState.showSnackbar("Redo applied") }
        }
    }

    // Crop State
    var selectedCropRatio by remember { mutableStateOf(CropRatio.FREE) }
    var currentCropRect by remember { mutableStateOf<Rect?>(null) }
    var currentImageRect by remember { mutableStateOf<Rect?>(null) }

    // Convert Format State
    var targetFormat by remember { mutableStateOf("JPG") } // JPG, PNG, WEBP

    // Camera launcher setup
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(tempCameraUri!!)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) {
                            withContext(Dispatchers.Main) {
                                originalBitmap = bmp
                                editedBitmap = bmp
                                undoStack = emptyList()
                                redoStack = emptyList()
                                originalFileName = "CAMERA_${System.currentTimeMillis()}.jpg"
                                originalSizeBytes = bmp.byteCount.toLong()
                                originalFormat = "JPG"
                                targetWidthStr = bmp.width.toString()
                                targetHeightStr = bmp.height.toString()
                                savedUri = null
                                snackbarHostState.showSnackbar("Camera photo loaded!")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val launchCameraSafely = {
        val uri = FileUtil.createTempImageUri(context)
        if (uri != null) {
            tempCameraUri = uri
            try {
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                scope.launch { snackbarHostState.showSnackbar("Camera application not available.") }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Could not initialize camera storage.") }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraSafely()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Camera permission is required to take photos.") }
        }
    }

    // Gallery Picker launcher setup
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            scope.launch(Dispatchers.IO) {
                try {
                    val fileName = FileUtil.getFileNameFromUri(context, uri)
                    val ext = if (fileName.contains(".")) fileName.substringAfterLast('.').uppercase() else "JPG"

                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) {
                            val tempFile = FileUtil.getFileFromUri(context, uri)
                            val size = tempFile?.length() ?: (bmp.byteCount / 3L)

                            withContext(Dispatchers.Main) {
                                originalBitmap = bmp
                                editedBitmap = bmp
                                undoStack = emptyList()
                                redoStack = emptyList()
                                originalFileName = fileName
                                originalSizeBytes = size
                                originalFormat = ext
                                targetWidthStr = bmp.width.toString()
                                targetHeightStr = bmp.height.toString()
                                savedUri = null
                                snackbarHostState.showSnackbar("Loaded: $fileName")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Calculations for Original & Edited Image info
    val hasImage = originalBitmap != null
    val origWidth = originalBitmap?.width ?: 0
    val origHeight = originalBitmap?.height ?: 0
    val origSizeStr = if (hasImage) FileUtil.getFileSizeString(originalSizeBytes) else "-"

    val editWidth = editedBitmap?.width ?: 0
    val editHeight = editedBitmap?.height ?: 0

    // Real Compression size estimation based on quality & dimensions
    val estimatedBytes = remember(originalSizeBytes, editWidth, editHeight, origWidth, origHeight, compressionQuality, activeToolMode, targetFormat, hasImage) {
        if (!hasImage) 0L
        else {
            val dimensionFactor = (editWidth.toDouble() * editHeight) / (origWidth.toDouble() * origHeight).coerceAtLeast(1.0)
            val qualityFactor = (compressionQuality / 100.0).coerceIn(0.15, 1.0)
            val formatFactor = when (targetFormat) {
                "WEBP" -> 0.75
                "PNG" -> 1.2
                else -> 0.85
            }
            val estimated = (originalSizeBytes * dimensionFactor * qualityFactor * formatFactor).toLong()
            estimated.coerceAtLeast(15000L)
        }
    }

    val estimatedSizeStr = if (hasImage) FileUtil.getFileSizeString(estimatedBytes) else "-"
    val savedPercent = remember(originalSizeBytes, estimatedBytes, hasImage) {
        if (!hasImage || originalSizeBytes <= 0) 0
        else {
            val pct = (((originalSizeBytes - estimatedBytes).toDouble() / originalSizeBytes) * 100).toInt()
            pct.coerceAtLeast(1)
        }
    }

    // Main Save Processing Action
    fun processAndSaveImage(onSaved: ((Uri) -> Unit)? = null) {
        val bmp = editedBitmap ?: originalBitmap
        if (bmp == null) {
            scope.launch { snackbarHostState.showSnackbar("Please select an image first.") }
            return
        }

        scope.launch(Dispatchers.IO) {
            val formatEnum = when (targetFormat) {
                "PNG" -> Bitmap.CompressFormat.PNG
                "WEBP" -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }

            val outStream = ByteArrayOutputStream()
            bmp.compress(formatEnum, compressionQuality.toInt(), outStream)
            val bytes = outStream.toByteArray()
            val compressedBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: bmp

            val timeStamp = System.currentTimeMillis()
            val nameNoExt = if (originalFileName.contains(".")) originalFileName.substringBeforeLast(".") else originalFileName
            val saveName = "${if (nameNoExt.isNotEmpty()) nameNoExt else "IMG"}_edited_$timeStamp"

            val uri = FileUtil.saveBitmapToGallery(context, compressedBmp, saveName, formatEnum)

            withContext(Dispatchers.Main) {
                if (uri != null) {
                    savedUri = uri
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar("Image saved successfully to Pictures/MuftTools!")
                    onSaved?.invoke(uri)
                } else {
                    snackbarHostState.showSnackbar("Failed to save image. Check storage permissions.")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Image Tools",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { performUndo() },
                        enabled = undoStack.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) TextPrimary else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = { performRedo() },
                        enabled = redoStack.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) TextPrimary else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleFavorite()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Crown",
                            tint = CrownGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurpleDarkBg)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Matching Bottom Navigation Bar (Home, Favorites, History)
            Surface(
                color = SurfaceCardBg,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, CardBorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(icon = Icons.Default.Home, label = "Home", isSelected = true, onClick = onBackClick)
                    BottomNavItem(icon = Icons.Default.FavoriteBorder, label = "Favorites", isSelected = isFavorite, onClick = onToggleFavorite)
                    BottomNavItem(icon = Icons.Default.History, label = "History", isSelected = false, onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Saved images are in Pictures/MuftTools gallery.") }
                    })
                }
            }
        },
        containerColor = PurpleDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {

                // 1. Image Preview Box (Fixed at top)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(18.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        if (originalBitmap == null) {
                            // Placeholder State matching reference design
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawDashedBorder(DashedBorderColor, 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(PurplePrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = PurpleGlow,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Text(
                                        text = "Tap to select an image",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Supports JPG, PNG, WebP, BMP",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Gallery", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                                    context,
                                                    android.Manifest.permission.CAMERA
                                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                                if (hasPermission) {
                                                    launchCameraSafely()
                                                } else {
                                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282046)),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, CardBorderColor)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Camera", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (activeToolMode == ToolMode.CROP) {
                                InteractiveCropEditor(
                                    bitmap = editedBitmap ?: originalBitmap!!,
                                    cropRatio = selectedCropRatio,
                                    onCropRectChanged = { cropR, imgR ->
                                        currentCropRect = cropR
                                        currentImageRect = imgR
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Single Image Preview (shows the currently edited or loaded picture)
                                Image(
                                    bitmap = (editedBitmap ?: originalBitmap!!).asImageBitmap(),
                                    contentDescription = "Selected Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { galleryLauncher.launch("image/*") },
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Floating Undo/Redo Overlay Controls
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { performUndo() },
                                    enabled = undoStack.isNotEmpty(),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = "Undo",
                                        tint = if (undoStack.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (undoStack.isNotEmpty()) {
                                    Text(
                                        text = "${undoStack.size}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { performRedo() },
                                    enabled = redoStack.isNotEmpty(),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Redo,
                                        contentDescription = "Redo",
                                        tint = if (redoStack.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Toolbar Action Buttons Bar (Fixed right below preview)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToolModeItem(
                        icon = Icons.Default.Crop,
                        label = "Crop",
                        isSelected = activeToolMode == ToolMode.CROP,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeToolMode = ToolMode.CROP
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.AspectRatio,
                        label = "Resize",
                        isSelected = activeToolMode == ToolMode.RESIZE,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeToolMode = ToolMode.RESIZE
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.Download,
                        label = "Compress",
                        isSelected = activeToolMode == ToolMode.COMPRESS,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeToolMode = ToolMode.COMPRESS
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.Refresh,
                        label = "Convert",
                        isSelected = activeToolMode == ToolMode.CONVERT,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeToolMode = ToolMode.CONVERT
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.RotateRight,
                        label = "Rotate",
                        isSelected = activeToolMode == ToolMode.ROTATE,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            activeToolMode = ToolMode.ROTATE
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Lower Content Section (Image Info, tool settings, Quality slider, Save & Share)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                // 3. Image Info Card (Matching Reference Layout)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Image Info",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Original Image Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF201A38))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Original Image", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (hasImage) "Name: $originalFileName" else "Name: -",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (hasImage) "Size: $origSizeStr" else "Size: -",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = if (hasImage) "Dimensions: $origWidth x $origHeight" else "Dimensions: -",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = if (hasImage) "Format: $originalFormat" else "Format: -",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF2E264E))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(if (hasImage) "$origWidth x $origHeight" else "-", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Central Arrow Circle
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Edited Image Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF201A38))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Edited Image", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (hasImage) "Size: $estimatedSizeStr" else "Size: -",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (hasImage) "Dimensions: $editWidth x $editHeight" else "Dimensions: -",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = if (hasImage) "Format: $targetFormat" else "Format: -",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PurplePrimary.copy(alpha = 0.3f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(if (hasImage) "$editWidth x $editHeight" else "-", color = PurpleGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Quality & Estimated Size Controls
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Quality", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                val qualityLabel = when {
                                    compressionQuality >= 80f -> "High"
                                    compressionQuality >= 50f -> "Medium"
                                    else -> "Low"
                                }
                                Text("${compressionQuality.toInt()}% ($qualityLabel)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Slider(
                                value = compressionQuality,
                                onValueChange = { compressionQuality = it },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = PurpleGlow,
                                    activeTrackColor = PurpleGlow,
                                    inactiveTrackColor = Color(0xFF2B2245)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Estimated Size", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (hasImage) "~$estimatedSizeStr ($savedPercent% Smaller)" else "-",
                                    color = if (hasImage) GreenSavedText else TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 4. Mode Options Box (Switches depending on activeToolMode)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (activeToolMode) {
                            ToolMode.CROP -> {
                                Text("Crop Frame & Aspect Ratio", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                // Aspect Ratio Options Chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CropRatio.values().forEach { ratio ->
                                        val isSel = selectedCropRatio == ratio
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) PurplePrimary else Color(0xFF201A38))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedCropRatio = ratio
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ratio.displayName,
                                                color = if (isSel) Color.White else TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Action Buttons: Apply Crop & Reset Crop
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val cr = currentCropRect
                                            val ir = currentImageRect
                                            val srcBmp = editedBitmap ?: originalBitmap
                                            if (cr != null && ir != null && srcBmp != null && ir.width > 0 && ir.height > 0) {
                                                try {
                                                    val normLeft = ((cr.left - ir.left) / ir.width).coerceInSafe(0f, 1f)
                                                    val normTop = ((cr.top - ir.top) / ir.height).coerceInSafe(0f, 1f)
                                                    val normRight = ((cr.right - ir.left) / ir.width).coerceInSafe(0f, 1f)
                                                    val normBottom = ((cr.bottom - ir.top) / ir.height).coerceInSafe(0f, 1f)

                                                    var cropX = (normLeft * srcBmp.width).toInt().coerceIn(0, (srcBmp.width - 1).coerceAtLeast(0))
                                                    var cropY = (normTop * srcBmp.height).toInt().coerceIn(0, (srcBmp.height - 1).coerceAtLeast(0))
                                                    var cropW = ((normRight - normLeft) * srcBmp.width).toInt()
                                                    var cropH = ((normBottom - normTop) * srcBmp.height).toInt()

                                                    if (cropX + cropW > srcBmp.width) {
                                                        cropW = srcBmp.width - cropX
                                                    }
                                                    if (cropY + cropH > srcBmp.height) {
                                                        cropH = srcBmp.height - cropY
                                                    }
                                                    cropW = cropW.coerceIn(1, (srcBmp.width - cropX).coerceAtLeast(1))
                                                    cropH = cropH.coerceIn(1, (srcBmp.height - cropY).coerceAtLeast(1))

                                                    if (cropW > 0 && cropH > 0 && cropX >= 0 && cropY >= 0 && (cropX + cropW <= srcBmp.width) && (cropY + cropH <= srcBmp.height)) {
                                                        pushToUndo(srcBmp)
                                                        val cropped = Bitmap.createBitmap(srcBmp, cropX, cropY, cropW, cropH)
                                                        editedBitmap = cropped
                                                        targetWidthStr = cropped.width.toString()
                                                        targetHeightStr = cropped.height.toString()
                                                        scope.launch { snackbarHostState.showSnackbar("Crop applied!") }
                                                    } else {
                                                        scope.launch { snackbarHostState.showSnackbar("Invalid crop area selected") }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    scope.launch { snackbarHostState.showSnackbar("Could not crop image: ${e.localizedMessage ?: "Error"}") }
                                                }
                                            } else if (originalBitmap == null) {
                                                scope.launch { snackbarHostState.showSnackbar("Please select an image first") }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Apply Crop", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (originalBitmap != null) {
                                                if (editedBitmap != null && editedBitmap != originalBitmap) {
                                                    pushToUndo(editedBitmap)
                                                }
                                                editedBitmap = originalBitmap
                                                selectedCropRatio = CropRatio.FREE
                                                targetWidthStr = originalBitmap!!.width.toString()
                                                targetHeightStr = originalBitmap!!.height.toString()
                                                scope.launch { snackbarHostState.showSnackbar("Crop reset to original image") }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF201A38)),
                                        border = BorderStroke(1.dp, CardBorderColor),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            ToolMode.RESIZE -> {
                                Text("Resize Dimensions", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                // Quick Scale Presets
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(25, 50, 75, 100).forEach { pct ->
                                        val isSel = editedBitmap != null && (editedBitmap!!.width == (origWidth * pct / 100))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) PurplePrimary else Color(0xFF201A38))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    originalBitmap?.let { bmp ->
                                                        val src = editedBitmap ?: bmp
                                                        val nw = (bmp.width * pct / 100).coerceAtLeast(10)
                                                        val nh = (bmp.height * pct / 100).coerceAtLeast(10)
                                                        pushToUndo(src)
                                                        editedBitmap = Bitmap.createScaledBitmap(bmp, nw, nh, true)
                                                        targetWidthStr = nw.toString()
                                                        targetHeightStr = nh.toString()
                                                        scope.launch { snackbarHostState.showSnackbar("Resized to $pct%") }
                                                    }
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$pct%", color = if (isSel) Color.White else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = targetWidthStr,
                                        onValueChange = {
                                            targetWidthStr = it
                                            val w = it.toIntOrNull()
                                            val baseBmp = editedBitmap ?: originalBitmap
                                            if (w != null && w > 0 && baseBmp != null) {
                                                val h = if (maintainAspect) (w * baseBmp.height / baseBmp.width) else targetHeightStr.toIntOrNull() ?: baseBmp.height
                                                targetHeightStr = h.toString()
                                            }
                                        },
                                        label = { Text("Width (px)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PurpleGlow,
                                            unfocusedBorderColor = CardBorderColor,
                                            focusedLabelColor = PurpleGlow
                                        ),
                                        singleLine = true
                                    )

                                    Text("x", color = TextMuted, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = targetHeightStr,
                                        onValueChange = {
                                            targetHeightStr = it
                                            val h = it.toIntOrNull()
                                            val baseBmp = editedBitmap ?: originalBitmap
                                            if (h != null && h > 0 && baseBmp != null) {
                                                val w = if (maintainAspect) (h * baseBmp.width / baseBmp.height) else targetWidthStr.toIntOrNull() ?: baseBmp.width
                                                targetWidthStr = w.toString()
                                            }
                                        },
                                        label = { Text("Height (px)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PurpleGlow,
                                            unfocusedBorderColor = CardBorderColor,
                                            focusedLabelColor = PurpleGlow
                                        ),
                                        singleLine = true
                                    )
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val src = editedBitmap ?: originalBitmap
                                        val w = targetWidthStr.toIntOrNull()
                                        val h = targetHeightStr.toIntOrNull()
                                        if (w != null && w > 0 && h != null && h > 0 && src != null) {
                                            pushToUndo(src)
                                            editedBitmap = Bitmap.createScaledBitmap(src, w, h, true)
                                            scope.launch { snackbarHostState.showSnackbar("Resized to ${w}x${h} px") }
                                        } else if (originalBitmap == null) {
                                            scope.launch { snackbarHostState.showSnackbar("Please select an image first") }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply Resize", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            ToolMode.COMPRESS -> {
                                Text("Quality Compression Level", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Adjust the quality slider under Image Info to compress image file size.", color = TextMuted, fontSize = 12.sp)
                            }

                            ToolMode.CONVERT -> {
                                Text("Convert Output Format", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                val formats = listOf("JPG", "PNG", "WEBP")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    formats.forEach { fmt ->
                                        val isSel = targetFormat == fmt
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSel) PurplePrimary else Color(0xFF201A38))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    targetFormat = fmt
                                                }
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (isSel) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                                Text(fmt, color = if (isSel) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            ToolMode.ROTATE -> {
                                Text("Rotate & Flip Controls", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val src = editedBitmap ?: originalBitmap
                                            src?.let { bmp ->
                                                pushToUndo(bmp)
                                                val matrix = Matrix().apply { postRotate(90f) }
                                                val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                                editedBitmap = rotated
                                                targetWidthStr = rotated.width.toString()
                                                targetHeightStr = rotated.height.toString()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF201A38)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.RotateRight, contentDescription = null, tint = PurpleGlow)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("90°", color = TextPrimary, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val src = editedBitmap ?: originalBitmap
                                            src?.let { bmp ->
                                                pushToUndo(bmp)
                                                val matrix = Matrix().apply { postScale(-1f, 1f) }
                                                val flipped = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                                editedBitmap = flipped
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF201A38)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Flip H", color = TextPrimary, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val src = editedBitmap ?: originalBitmap
                                            src?.let { bmp ->
                                                pushToUndo(bmp)
                                                val matrix = Matrix().apply { postScale(1f, -1f) }
                                                val flipped = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                                editedBitmap = flipped
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF201A38)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Flip V", color = TextPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Action Buttons ("Save Image" & "Share Image")
                Button(
                    onClick = { processAndSaveImage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Save Image",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (savedUri != null) {
                            FileUtil.shareFile(context, savedUri!!, "image/*", "Share Image")
                        } else {
                            processAndSaveImage { uri ->
                                FileUtil.shareFile(context, uri, "image/*", "Share Image")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF201A38)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Share Image",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Generous bottom space so Save and Share buttons are never obscured by bottom bar
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
}

// Custom Interactive Tool Mode Item Button
@Composable
private fun ToolModeItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SurfaceCardActive else SurfaceCardBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) ActiveBorderColor else CardBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PurpleGlow else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// Custom Dashed Border Extension for Placeholders
private fun Modifier.drawDashedBorder(color: Color, radius: androidx.compose.ui.unit.Dp): Modifier = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx(), radius.toPx())
    )
}

// Bottom Navigation Item
@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PurpleGlow else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isSelected) PurpleGlow else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

