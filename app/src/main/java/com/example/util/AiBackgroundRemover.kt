package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.Log
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

data class RuntimeVerification(
    val modelExistsInAssets: Boolean,
    val modelFileName: String,
    val loadedModelName: String,
    val inputTensorShape: String,
    val outputTensorShape: String,
    val usedFallback: Boolean,
    val loadErrorReason: String? = null,
    val logs: List<String> = emptyList()
)

data class SegmentationResult(
    val bitmap: Bitmap,
    val verification: RuntimeVerification
)

object AiBackgroundRemover {

    private const val TAG = "AiBackgroundRemover"
    private const val PRIMARY_MODEL = "isnet_birefnet.tflite"

    private var tfliteInterpreter: Interpreter? = null
    private var interpreterModelPath: String? = null
    private var lastLoadError: String? = null

    private fun loadModelFile(context: Context, modelName: String): ByteBuffer? {
        try {
            val fileDescriptor = context.assets.openFd(modelName)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val mappedBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            Log.i(TAG, "Successfully mapped $modelName via openFd (size=$declaredLength bytes)")
            return mappedBuffer
        } catch (e: Exception) {
            Log.w(TAG, "openFd failed for $modelName (${e.localizedMessage}), falling back to direct InputStream read")
        }

        return try {
            context.assets.open(modelName).use { inputStream ->
                val bytes = inputStream.readBytes()
                val buffer = ByteBuffer.allocateDirect(bytes.size)
                buffer.order(ByteOrder.nativeOrder())
                buffer.put(bytes)
                buffer.rewind()
                Log.i(TAG, "Successfully read $modelName into direct ByteBuffer (size=${bytes.size} bytes)")
                buffer
            }
        } catch (e: Exception) {
            val err = "Failed to load $modelName from assets: ${e.localizedMessage}"
            Log.e(TAG, err, e)
            lastLoadError = err
            null
        }
    }

    private fun checkAssetExists(context: Context, name: String): Boolean {
        return try {
            context.assets.open(name).use { true }
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    private fun getInterpreter(context: Context, logs: MutableList<String>): Interpreter? {
        val primaryExists = checkAssetExists(context, PRIMARY_MODEL)
        logs.add("Checking asset existence: $PRIMARY_MODEL -> exists=$primaryExists")
        Log.i(TAG, "Asset existence check: $PRIMARY_MODEL exists=$primaryExists")

        val modelToUse = when {
            primaryExists -> PRIMARY_MODEL
            checkAssetExists(context, "deeplab_v3.tflite") -> "deeplab_v3.tflite"
            else -> null
        }

        if (modelToUse == null) {
            val msg = "No TFLite segmentation model found in assets."
            logs.add("ERROR: $msg")
            Log.e(TAG, msg)
            lastLoadError = msg
            return null
        }

        if (tfliteInterpreter != null && interpreterModelPath == modelToUse) {
            logs.add("Reusing initialized interpreter for $modelToUse")
            return tfliteInterpreter
        }

        tfliteInterpreter?.close()
        tfliteInterpreter = null

        logs.add("Opening model file $modelToUse from assets...")
        val modelBuffer = loadModelFile(context, modelToUse)
        if (modelBuffer == null) {
            val err = "Failed to create MappedByteBuffer for $modelToUse"
            logs.add("ERROR: $err")
            Log.e(TAG, err)
            lastLoadError = err
            return null
        }

        val options = Interpreter.Options().apply {
            setNumThreads(Runtime.getRuntime().availableProcessors().coerceIn(2, 6))
            try {
                setUseNNAPI(true)
            } catch (e: Exception) {
                logs.add("NNAPI delegate flag set failed, using CPU threads")
            }
        }

        return try {
            Interpreter(modelBuffer, options).also {
                tfliteInterpreter = it
                interpreterModelPath = modelToUse
                logs.add("TensorFlow Lite Interpreter created successfully for $modelToUse (NNAPI/GPU)")
                Log.i(TAG, "TFLite Interpreter initialized successfully for $modelToUse")
                lastLoadError = null
            }
        } catch (e: Exception) {
            logs.add("Interpreter init with NNAPI failed (${e.localizedMessage}), retrying with CPU options...")
            val cpuOptions = Interpreter.Options().apply {
                setNumThreads(Runtime.getRuntime().availableProcessors().coerceIn(2, 6))
            }
            try {
                Interpreter(modelBuffer, cpuOptions).also {
                    tfliteInterpreter = it
                    interpreterModelPath = modelToUse
                    logs.add("TensorFlow Lite Interpreter created successfully for $modelToUse (Pure CPU)")
                    Log.i(TAG, "TFLite Interpreter initialized successfully for $modelToUse on CPU")
                    lastLoadError = null
                }
            } catch (cpuEx: Exception) {
                val err = "Interpreter initialization failed for $modelToUse: ${cpuEx.localizedMessage}"
                logs.add("ERROR: $err")
                Log.e(TAG, err, cpuEx)
                lastLoadError = err
                null
            }
        }
    }

    fun verifyRuntimeState(context: Context): RuntimeVerification {
        val logs = mutableListOf<String>()
        val primaryExists = checkAssetExists(context, PRIMARY_MODEL)
        logs.add("Asset verification: $PRIMARY_MODEL exists=$primaryExists")

        val interpreter = getInterpreter(context, logs)
        if (interpreter != null) {
            val inputShape = interpreter.getInputTensor(0).shape().contentToString()
            val outputShape = interpreter.getOutputTensor(0).shape().contentToString()
            logs.add("Input Tensor Shape: $inputShape")
            logs.add("Output Tensor Shape: $outputShape")
            logs.add("Active Engine: TFLite ($interpreterModelPath)")

            return RuntimeVerification(
                modelExistsInAssets = primaryExists,
                modelFileName = PRIMARY_MODEL,
                loadedModelName = interpreterModelPath ?: "Unknown",
                inputTensorShape = inputShape,
                outputTensorShape = outputShape,
                usedFallback = false,
                loadErrorReason = null,
                logs = logs
            )
        } else {
            logs.add("Primary TFLite engine initialization failed. ML Kit Selfie Segmenter fallback will be used.")
            return RuntimeVerification(
                modelExistsInAssets = primaryExists,
                modelFileName = PRIMARY_MODEL,
                loadedModelName = "ML Kit Selfie Segmenter (Fallback)",
                inputTensorShape = "Dynamic Bitmap",
                outputTensorShape = "Dynamic Float Mask",
                usedFallback = true,
                loadErrorReason = lastLoadError ?: "Failed to initialize TFLite interpreter",
                logs = logs
            )
        }
    }

    suspend fun removeBackground(
        context: Context,
        bitmap: Bitmap,
        threshold: Float,
        bgStyleIndex: Int
    ): SegmentationResult = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        val primaryExists = checkAssetExists(context, PRIMARY_MODEL)
        logs.add("Starting background removal run...")
        logs.add("Primary asset $PRIMARY_MODEL present in APK: $primaryExists")

        val interpreter = getInterpreter(context, logs)

        if (interpreter != null) {
            try {
                val inputShape = interpreter.getInputTensor(0).shape()
                val outputShape = interpreter.getOutputTensor(0).shape()
                val inputShapeStr = inputShape.contentToString()
                val outputShapeStr = outputShape.contentToString()

                logs.add("Loaded model name: $interpreterModelPath")
                logs.add("Input tensor shape: $inputShapeStr")
                logs.add("Output tensor shape: $outputShapeStr")
                logs.add("Executing TFLite inference...")

                Log.i(TAG, "Executing inference with model: $interpreterModelPath | input=$inputShapeStr | output=$outputShapeStr")

                val outputBmp = runTfliteSegmentation(
                    interpreter = interpreter,
                    originalBitmap = bitmap,
                    threshold = threshold,
                    bgStyleIndex = bgStyleIndex,
                    inputShape = inputShape,
                    outputShape = outputShape
                )

                logs.add("Inference completed successfully without fallback!")
                Log.i(TAG, "Inference completed successfully with TFLite model $interpreterModelPath")

                val verification = RuntimeVerification(
                    modelExistsInAssets = primaryExists,
                    modelFileName = PRIMARY_MODEL,
                    loadedModelName = interpreterModelPath ?: "TFLite Model",
                    inputTensorShape = inputShapeStr,
                    outputTensorShape = outputShapeStr,
                    usedFallback = false,
                    loadErrorReason = null,
                    logs = logs
                )

                return@withContext SegmentationResult(outputBmp, verification)
            } catch (e: Exception) {
                val err = "TFLite inference error: ${e.localizedMessage}"
                logs.add("ERROR: $err")
                Log.e(TAG, err, e)
                lastLoadError = err
            }
        }

        logs.add("Falling back to ML Kit Selfie Segmenter...")
        Log.w(TAG, "Falling back to ML Kit Selfie Segmenter due to TFLite error or unavailability.")

        val fallbackBmp = runMlKitSegmentation(bitmap, threshold, bgStyleIndex)
        logs.add("ML Kit inference completed.")

        val verification = RuntimeVerification(
            modelExistsInAssets = primaryExists,
            modelFileName = PRIMARY_MODEL,
            loadedModelName = "ML Kit Selfie Segmenter (Fallback)",
            inputTensorShape = "[${bitmap.width}, ${bitmap.height}, 4]",
            outputTensorShape = "[${bitmap.width}, ${bitmap.height}]",
            usedFallback = true,
            loadErrorReason = lastLoadError ?: "TFLite unavailable",
            logs = logs
        )

        return@withContext SegmentationResult(fallbackBmp, verification)
    }

    private fun runTfliteSegmentation(
        interpreter: Interpreter,
        originalBitmap: Bitmap,
        threshold: Float,
        bgStyleIndex: Int,
        inputShape: IntArray,
        outputShape: IntArray
    ): Bitmap {
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

        // Allocate output buffer
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
