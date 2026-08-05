package com.mufttools.scanner.util

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

enum class ScanFilter {
    ORIGINAL,
    AUTO,
    MAGIC_COLOR,
    GRAYSCALE,
    BLACK_AND_WHITE
}

object DocumentProcessor {

    fun detectDocumentCorners(bitmap: Bitmap): List<Offset> {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.gaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

        val edged = Mat()
        Imgproc.Canny(gray, edged, 75.0, 200.0)

        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edged, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        var maxArea = 0.0
        var maxContour: MatOfPoint2f? = null

        for (contour in contours) {
            val c2f = MatOfPoint2f(*contour.toArray())
            val area = Imgproc.contourArea(c2f)
            if (area > 500) {
                val peri = Imgproc.arcLength(c2f, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)

                if (approx.total() == 4L && area > maxArea) {
                    maxArea = area
                    maxContour = approx
                }
            }
        }

        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()

        val defaultCorners = listOf(
            Offset(0.1f * width, 0.1f * height),
            Offset(0.9f * width, 0.1f * height),
            Offset(0.9f * width, 0.9f * height),
            Offset(0.1f * width, 0.9f * height)
        )

        if (maxContour == null) {
            mat.release()
            gray.release()
            edged.release()
            return defaultCorners
        }

        val points = maxContour.toArray().map { Offset(it.x.toFloat(), it.y.toFloat()) }
        mat.release()
        gray.release()
        edged.release()

        return orderPoints(points)
    }

    fun cropAndWarp(bitmap: Bitmap, corners: List<Offset>): Bitmap {
        val srcMat = Mat()
        Utils.bitmapToMat(bitmap, srcMat)

        val ordered = orderPoints(corners)
        val tl = ordered[0]
        val tr = ordered[1]
        val br = ordered[2]
        val bl = ordered[3]

        val widthA = sqrt((br.x - bl.x) * (br.x - bl.x) + (br.y - bl.y) * (br.y - bl.y))
        val widthB = sqrt((tr.x - tl.x) * (tr.x - tl.x) + (tr.y - tl.y) * (tr.y - tl.y))
        val maxWidth = max(widthA, widthB).toDouble()

        val heightA = sqrt((tr.x - br.x) * (tr.x - br.x) + (tr.y - br.y) * (tr.y - br.y))
        val heightB = sqrt((tl.x - bl.x) * (tl.x - bl.x) + (tl.y - bl.y) * (tl.y - bl.y))
        val maxHeight = max(heightA, heightB).toDouble()

        val srcMatPoint = MatOfPoint2f(
            Point(tl.x.toDouble(), tl.y.toDouble()),
            Point(tr.x.toDouble(), tr.y.toDouble()),
            Point(br.x.toDouble(), br.y.toDouble()),
            Point(bl.x.toDouble(), bl.y.toDouble())
        )

        val dstMatPoint = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(maxWidth - 1, 0.0),
            Point(maxWidth - 1, maxHeight - 1),
            Point(0.0, maxHeight - 1)
        )

        val transform = Imgproc.getPerspectiveTransform(srcMatPoint, dstMatPoint)
        val destMat = Mat()
        Imgproc.warpPerspective(srcMat, destMat, transform, Size(maxWidth, maxHeight))

        val resultBitmap = Bitmap.createBitmap(maxWidth.toInt(), maxHeight.toInt(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(destMat, resultBitmap)

        srcMat.release()
        destMat.release()
        transform.release()

        return resultBitmap
    }

    fun applyFilter(bitmap: Bitmap, filter: ScanFilter): Bitmap {
        if (filter == ScanFilter.ORIGINAL) return bitmap

        val srcMat = Mat()
        Utils.bitmapToMat(bitmap, srcMat)
        val destMat = Mat()

        when (filter) {
            ScanFilter.GRAYSCALE -> {
                Imgproc.cvtColor(srcMat, destMat, Imgproc.COLOR_RGBA2GRAY)
                Imgproc.cvtColor(destMat, destMat, Imgproc.COLOR_GRAY2RGBA)
            }
            ScanFilter.BLACK_AND_WHITE -> {
                val gray = Mat()
                Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_RGBA2GRAY)
                Imgproc.adaptiveThreshold(
                    gray,
                    destMat,
                    255.0,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY,
                    15,
                    10.0
                )
                Imgproc.cvtColor(destMat, destMat, Imgproc.COLOR_GRAY2RGBA)
                gray.release()
            }
            ScanFilter.MAGIC_COLOR -> {
                val lab = Mat()
                Imgproc.cvtColor(srcMat, lab, Imgproc.COLOR_RGBA2RGB)
                Imgproc.cvtColor(lab, lab, Imgproc.COLOR_RGB2Lab)
                val channels = ArrayList<Mat>()
                Core.split(lab, channels)

                val clahe = Imgproc.createCLAHE(3.0, Size(8.0, 8.0))
                clahe.apply(channels[0], channels[0])

                Core.merge(channels, lab)
                Imgproc.cvtColor(lab, destMat, Imgproc.COLOR_Lab2RGBA)
                lab.release()
            }
            ScanFilter.AUTO -> {
                srcMat.convertTo(destMat, -1, 1.2, 10.0)
            }
            ScanFilter.ORIGINAL -> {}
        }

        val resultBitmap = Bitmap.createBitmap(destMat.cols(), destMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(destMat, resultBitmap)

        srcMat.release()
        destMat.release()

        return resultBitmap
    }

    private fun orderPoints(points: List<Offset>): List<Offset> {
        val sortedBySum = points.sortedBy { it.x + it.y }
        val tl = sortedBySum.first()
        val br = sortedBySum.last()

        val remaining = points.filter { it != tl && it != br }
        val sortedByDiff = remaining.sortedBy { it.y - it.x }
        val tr = sortedByDiff.first()
        val bl = sortedByDiff.last()

        return listOf(tl, tr, br, bl)
    }
}
