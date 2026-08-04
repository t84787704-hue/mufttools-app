package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import kotlin.math.max
import kotlin.math.min

enum class ImageFilterType(val label: String) {
    ORIGINAL("Original"),
    COLOR("Color"),
    BLACK_AND_WHITE("B & W"),
    MAGIC("Magic"),
    GRAYSCALE("Grayscale")
}

data class CropCorners(
    val topLeft: Offset = Offset(0.05f, 0.05f),
    val topRight: Offset = Offset(0.95f, 0.05f),
    val bottomRight: Offset = Offset(0.95f, 0.95f),
    val bottomLeft: Offset = Offset(0.05f, 0.95f)
)

object ImageProcessingUtil {

    fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        if (degrees % 360f == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun applyFilter(source: Bitmap, filter: ImageFilterType): Bitmap {
        return when (filter) {
            ImageFilterType.ORIGINAL -> source
            ImageFilterType.COLOR -> applyColorEnhance(source)
            ImageFilterType.BLACK_AND_WHITE -> applyBlackAndWhite(source)
            ImageFilterType.MAGIC -> applyMagicFilter(source)
            ImageFilterType.GRAYSCALE -> applyGrayscale(source)
        }
    }

    private fun applyGrayscale(source: Bitmap): Bitmap {
        val bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return bitmap
    }

    private fun applyColorEnhance(source: Bitmap): Bitmap {
        val bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val cm = ColorMatrix().apply {
            // Increase saturation and slight contrast boost
            setSaturation(1.3f)
        }
        val contrastMatrix = ColorMatrix(floatArrayOf(
            1.15f, 0f, 0f, 0f, -10f,
            0f, 1.15f, 0f, 0f, -10f,
            0f, 0f, 1.15f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(contrastMatrix)

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return bitmap
    }

    private fun applyBlackAndWhite(source: Bitmap): Bitmap {
        val bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // High contrast B&W matrix
        val matrix = ColorMatrix(floatArrayOf(
            85f, 85f, 85f, 0f, -25000f,
            85f, 85f, 85f, 0f, -25000f,
            85f, 85f, 85f, 0f, -25000f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return bitmap
    }

    private fun applyMagicFilter(source: Bitmap): Bitmap {
        val bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Document Magic Color: Whitens background, sharpens text contrast, boosts color vibrancy
        val matrix = ColorMatrix(floatArrayOf(
            1.5f, 0.2f, 0.0f, 0f, -40f,
            0.1f, 1.5f, 0.1f, 0f, -40f,
            0.0f, 0.2f, 1.5f, 0f, -40f,
            0.0f, 0.0f, 0.0f, 1f, 0f
        ))
        
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return bitmap
    }

    fun autoDetectEdges(bitmap: Bitmap): CropCorners {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return CropCorners()

        // Sample down for fast edge detection analysis
        val maxSample = 300
        val scale = min(1f, maxSample.toFloat() / max(width, height))
        val sampleW = (width * scale).toInt().coerceAtLeast(10)
        val sampleH = (height * scale).toInt().coerceAtLeast(10)

        val scaled = Bitmap.createScaledBitmap(bitmap, sampleW, sampleH, false)
        val pixels = IntArray(sampleW * sampleH)
        scaled.getPixels(pixels, 0, sampleW, 0, 0, sampleW, sampleH)

        var minX = sampleW
        var maxX = 0
        var minY = sampleH
        var maxY = 0

        // Find boundary pixels with high contrast / luminance variation from outer margins
        val threshold = 35
        for (y in 0 until sampleH) {
            for (x in 0 until sampleW) {
                val color = pixels[y * sampleW + x]
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

                // Border gradient variance estimate
                val rightPixel = if (x + 1 < sampleW) pixels[y * sampleW + x + 1] else color
                val rR = (rightPixel shr 16) and 0xFF
                val rG = (rightPixel shr 8) and 0xFF
                val rB = rightPixel and 0xFF
                val rightGray = (0.299 * rR + 0.587 * rG + 0.114 * rB).toInt()

                if (kotlin.math.abs(gray - rightGray) > threshold) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        // Clean up or fallback if no distinct edge bounding box detected
        if (minX >= maxX || minY >= maxY || (maxX - minX) < sampleW * 0.2f || (maxY - minY) < sampleH * 0.2f) {
            return CropCorners(
                topLeft = Offset(0.08f, 0.08f),
                topRight = Offset(0.92f, 0.08f),
                bottomRight = Offset(0.92f, 0.92f),
                bottomLeft = Offset(0.08f, 0.92f)
            )
        }

        // Add slight margin around auto detected edge bounding box
        val padX = (maxX - minX) * 0.02f
        val padY = (maxY - minY) * 0.02f

        val leftNorm = ((minX - padX) / sampleW).coerceIn(0.02f, 0.98f)
        val rightNorm = ((maxX + padX) / sampleW).coerceIn(0.02f, 0.98f)
        val topNorm = ((minY - padY) / sampleH).coerceIn(0.02f, 0.98f)
        val bottomNorm = ((maxY + padY) / sampleH).coerceIn(0.02f, 0.98f)

        return CropCorners(
            topLeft = Offset(leftNorm, topNorm),
            topRight = Offset(rightNorm, topNorm),
            bottomRight = Offset(rightNorm, bottomNorm),
            bottomLeft = Offset(leftNorm, bottomNorm)
        )
    }

    fun cropBitmap(source: Bitmap, corners: CropCorners): Bitmap {
        val width = source.width
        val height = source.height

        val leftX = (min(corners.topLeft.x, corners.bottomLeft.x) * width).toInt().coerceIn(0, width - 1)
        val topY = (min(corners.topLeft.y, corners.topRight.y) * height).toInt().coerceIn(0, height - 1)
        val rightX = (max(corners.topRight.x, corners.bottomRight.x) * width).toInt().coerceIn(leftX + 1, width)
        val bottomY = (max(corners.bottomLeft.y, corners.bottomRight.y) * height).toInt().coerceIn(topY + 1, height)

        val cropWidth = (rightX - leftX).coerceAtLeast(1)
        val cropHeight = (bottomY - topY).coerceAtLeast(1)

        // Create standard cropped sub-bitmap
        val cropped = Bitmap.createBitmap(source, leftX, topY, cropWidth, cropHeight)

        // Perspective transform using Android Matrix setPolyToPoly if non-rectangular
        val srcPoly = floatArrayOf(
            corners.topLeft.x * width - leftX, corners.topLeft.y * height - topY,
            corners.topRight.x * width - leftX, corners.topRight.y * height - topY,
            corners.bottomRight.x * width - leftX, corners.bottomRight.y * height - topY,
            corners.bottomLeft.x * width - leftX, corners.bottomLeft.y * height - topY
        )

        val destPoly = floatArrayOf(
            0f, 0f,
            cropWidth.toFloat(), 0f,
            cropWidth.toFloat(), cropHeight.toFloat(),
            0f, cropHeight.toFloat()
        )

        val matrix = Matrix()
        if (matrix.setPolyToPoly(srcPoly, 0, destPoly, 0, 4)) {
            val result = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawBitmap(cropped, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            return result
        }

        return cropped
    }
}
