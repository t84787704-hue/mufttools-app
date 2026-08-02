package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.coroutines.resume
import kotlin.math.min

object AiBackgroundRemover {

    private var tfliteInterpreter: Interpreter? = null
    private var interpreterModelPath: String? = null

    private fun loadModelFile(context: Context, modelName: String): ByteBuffer? {
        return try {
            val fileDescriptor = context.assets.openFd(modelName)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Synchronized
    private fun getInterpreter(context: Context): Interpreter? {
        val modelToUse = when {
            hasAsset(context, "isnet_birefnet.tflite") -> "isnet_birefnet.tflite"
            hasAsset(context, "deeplab_v3.tflite") -> "deeplab_v3.tflite"
            else -> null
        } ?: return null

        if (tfliteInterpreter != null && interpreterModelPath == modelToUse) {
            return tfliteInterpreter
        }

        tfliteInterpreter?.close()
        tfliteInterpreter = null

        val modelBuffer = loadModelFile(context, modelToUse) ?: return null
        val options = Interpreter.Options().apply {
            setNumThreads(Runtime.getRuntime().availableProcessors().coerceIn(2, 6))
            try {
                setUseNNAPI(true)
            } catch (ignored: Exception) {}
        }

        return try {
            Interpreter(modelBuffer, options).also {
                tfliteInterpreter = it
                interpreterModelPath = modelToUse
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun hasAsset(context: Context, name: String): Boolean {
        return try {
            context.assets.open(name).use { true }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeBackground(
        context: Context,
        bitmap: Bitmap,
        threshold: Float,
        bgStyleIndex: Int
    ): Bitmap = withContext(Dispatchers.IO) {
        val interpreter = getInterpreter(context)
        if (interpreter != null) {
            try {
                return@withContext runTfliteSegmentation(interpreter, bitmap, threshold, bgStyleIndex)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to ML Kit Selfie Segmenter
        return@withContext runMlKitSegmentation(bitmap, threshold, bgStyleIndex)
    }

    private fun runTfliteSegmentation(
        interpreter: Interpreter,
        originalBitmap: Bitmap,
        threshold: Float,
        bgStyleIndex: Int
    ): Bitmap {
        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()

        var modelWidth = 512
        var modelHeight = 512
        var isNCHW = false

        if (inputShape.size == 4) {
            if (inputShape[1] == 3 || inputShape[1] == 1) {
                isNCHW = true
                modelHeight = if (inputShape[2] > 0) inputShape[2] else 512
                modelWidth = if (inputShape[3] > 0) inputShape[3] else 512
            } else {
                modelHeight = if (inputShape[1] > 0) inputShape[1] else 512
                modelWidth = if (inputShape[2] > 0) inputShape[2] else 512
            }
        }

        val resizedInput = Bitmap.createScaledBitmap(originalBitmap, modelWidth, modelHeight, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 3 * modelWidth * modelHeight * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val pixels = IntArray(modelWidth * modelHeight)
        resizedInput.getPixels(pixels, 0, modelWidth, 0, 0, modelWidth, modelHeight)

        // Standard ImageNet Normalization
        if (isNCHW) {
            for (c in 0..2) {
                val mean = when(c) { 0 -> 0.485f; 1 -> 0.456f; else -> 0.406f }
                val std = when(c) { 0 -> 0.229f; 1 -> 0.224f; else -> 0.225f }
                for (i in 0 until modelWidth * modelHeight) {
                    val color = pixels[i]
                    val channelVal = when(c) {
                        0 -> (color shr 16) and 0xFF
                        1 -> (color shr 8) and 0xFF
                        else -> color and 0xFF
                    }
                    val normalized = ((channelVal / 255.0f) - mean) / std
                    inputBuffer.putFloat(normalized)
                }
            }
        } else {
            for (i in 0 until modelWidth * modelHeight) {
                val color = pixels[i]
                val r = (((color shr 16) and 0xFF) / 255.0f - 0.485f) / 0.229f
                val g = (((color shr 8) and 0xFF) / 255.0f - 0.456f) / 0.224f
                val b = ((color and 0xFF) / 255.0f - 0.406f) / 0.225f
                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        inputBuffer.rewind()

        // Allocate flat output float array
        val totalOutputElements = outputShape.fold(1) { acc, dim -> acc * if (dim > 0) dim else 1 }
        val outputBuffer = ByteBuffer.allocateDirect(totalOutputElements * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val maskMap = Array(modelHeight) { FloatArray(modelWidth) }
        val floatBuffer = outputBuffer.asFloatBuffer()

        for (y in 0 until modelHeight) {
            for (x in 0 until modelWidth) {
                if (floatBuffer.hasRemaining()) {
                    maskMap[y][x] = floatBuffer.get()
                }
            }
        }

        val origWidth = originalBitmap.width
        val origHeight = originalBitmap.height
        val outputBitmap = Bitmap.createBitmap(origWidth, origHeight, Bitmap.Config.ARGB_8888)

        val origPixels = IntArray(origWidth * origHeight)
        originalBitmap.getPixels(origPixels, 0, origWidth, 0, 0, origWidth, origHeight)

        val bgIntColor = when (bgStyleIndex) {
            1 -> AndroidColor.WHITE
            2 -> AndroidColor.DKGRAY
            3 -> AndroidColor.rgb(30, 144, 255)
            else -> AndroidColor.TRANSPARENT
        }

        val featherRange = 0.08f
        val lowThresh = (threshold - featherRange).coerceAtLeast(0.01f)
        val highThresh = (threshold + featherRange).coerceAtMost(0.99f)

        // Bilinear scaling of mask array to full resolution original bitmap
        for (y in 0 until origHeight) {
            val my = (y.toFloat() / origHeight * modelHeight).coerceIn(0f, (modelHeight - 1).toFloat())
            val y0 = my.toInt()
            val y1 = min(y0 + 1, modelHeight - 1)
            val yLerp = my - y0

            for (x in 0 until origWidth) {
                val mx = (x.toFloat() / origWidth * modelWidth).coerceIn(0f, (modelWidth - 1).toFloat())
                val x0 = mx.toInt()
                val x1 = min(x0 + 1, modelWidth - 1)
                val xLerp = mx - x0

                val v00 = maskMap[y0][x0]
                val v01 = maskMap[y0][x1]
                val v10 = maskMap[y1][x0]
                val v11 = maskMap[y1][x1]

                val top = v00 + (v01 - v00) * xLerp
                val bot = v10 + (v11 - v10) * xLerp
                val prob = top + (bot - top) * yLerp

                val origPixel = origPixels[y * origWidth + x]

                if (prob < lowThresh) {
                    origPixels[y * origWidth + x] = bgIntColor
                } else if (prob > highThresh) {
                    origPixels[y * origWidth + x] = origPixel
                } else {
                    // Smooth alpha transition zone
                    val alphaRatio = (prob - lowThresh) / (highThresh - lowThresh)
                    val alpha = (alphaRatio * 255).toInt().coerceIn(0, 255)

                    if (bgStyleIndex == 0) { // Transparent background
                        val r = (origPixel shr 16) and 0xFF
                        val g = (origPixel shr 8) and 0xFF
                        val b = origPixel and 0xFF
                        origPixels[y * origWidth + x] = (alpha shl 24) or (r shl 16) or (g shl 8) or b
                    } else { // Solid background color blend
                        val bgR = (bgIntColor shr 16) and 0xFF
                        val bgG = (bgIntColor shr 8) and 0xFF
                        val bgB = bgIntColor and 0xFF

                        val fgR = (origPixel shr 16) and 0xFF
                        val fgG = (origPixel shr 8) and 0xFF
                        val fgB = origPixel and 0xFF

                        val blendR = (fgR * alphaRatio + bgR * (1f - alphaRatio)).toInt().coerceIn(0, 255)
                        val blendG = (fgG * alphaRatio + bgG * (1f - alphaRatio)).toInt().coerceIn(0, 255)
                        val blendB = (fgB * alphaRatio + bgB * (1f - alphaRatio)).toInt().coerceIn(0, 255)

                        origPixels[y * origWidth + x] = (0xFF shl 24) or (blendR shl 16) or (blendG shl 8) or blendB
                    }
                }
            }
        }

        outputBitmap.setPixels(origPixels, 0, origWidth, 0, 0, origWidth, origHeight)
        return outputBitmap
    }

    private suspend fun runMlKitSegmentation(
        bitmap: Bitmap,
        threshold: Float,
        bgStyleIndex: Int
    ): Bitmap = suspendCancellableCoroutine { continuation ->
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()
        val segmenter = Segmentation.getClient(options)
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        segmenter.process(inputImage)
            .addOnSuccessListener { segmentationMask ->
                val width = segmentationMask.width
                val height = segmentationMask.height
                val maskBuffer: ByteBuffer = segmentationMask.buffer

                val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val bgIntColor = when (bgStyleIndex) {
                    1 -> AndroidColor.WHITE
                    2 -> AndroidColor.DKGRAY
                    3 -> AndroidColor.rgb(30, 144, 255)
                    else -> AndroidColor.TRANSPARENT
                }

                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                maskBuffer.rewind()
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val bgConfidence = maskBuffer.float
                        val index = y * width + x
                        if (bgConfidence < threshold) {
                            pixels[index] = bgIntColor
                        }
                    }
                }

                outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                if (continuation.isActive) continuation.resume(outputBitmap)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(bitmap)
            }
    }
}
