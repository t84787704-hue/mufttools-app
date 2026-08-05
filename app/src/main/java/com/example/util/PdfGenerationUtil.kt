package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfGenerationUtil {

    suspend fun createPdfFromBitmaps(
        context: Context,
        bitmaps: List<Bitmap>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) return@withContext false

        val pdfDocument = PdfDocument()

        try {
            // Standard A4 dimensions in points (72 points/inch) -> 595 x 842
            val pageWidth = 595
            val pageHeight = 842

            bitmaps.forEachIndexed { index, bitmap ->
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // Draw white background
                canvas.drawColor(android.graphics.Color.WHITE)

                // Scale bitmap maintaining aspect ratio within A4 bounds with margin
                val margin = 20f
                val destWidth = pageWidth - (margin * 2)
                val destHeight = pageHeight - (margin * 2)

                val srcWidth = bitmap.width.toFloat()
                val srcHeight = bitmap.height.toFloat()

                val scale = Math.min(destWidth / srcWidth, destHeight / srcHeight)
                val finalWidth = srcWidth * scale
                val finalHeight = srcHeight * scale

                val left = margin + (destWidth - finalWidth) / 2f
                val top = margin + (destHeight - finalHeight) / 2f

                val destRect = RectF(left, top, left + finalWidth, top + finalHeight)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

                canvas.drawBitmap(bitmap, null, destRect, paint)
                pdfDocument.finishPage(page)
            }

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            pdfDocument.close()
        }
    }

    suspend fun renderPdfPageToBitmap(pdfFile: File, pageIndex: Int = 0): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (!pdfFile.exists()) return@withContext null
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            if (pageIndex >= renderer.pageCount) {
                renderer.close()
                fileDescriptor.close()
                return@withContext null
            }
            val page = renderer.openPage(pageIndex)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            fileDescriptor.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPdfPageCount(pdfFile: File): Int = withContext(Dispatchers.IO) {
        try {
            if (!pdfFile.exists()) return@withContext 0
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            val count = renderer.pageCount
            renderer.close()
            fileDescriptor.close()
            count
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
