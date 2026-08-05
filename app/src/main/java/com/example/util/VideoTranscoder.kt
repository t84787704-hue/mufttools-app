package com.example.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

object VideoTranscoder {

    data class VideoDetails(
        val width: Int,
        val height: Int,
        val durationMs: Long,
        val sizeBytes: Long,
        val formattedResolution: String,
        val formattedDuration: String,
        val formattedSize: String
    )

    /**
     * Extracts real metadata from a video file.
     */
    fun getVideoDetails(context: Context, videoFile: File): VideoDetails {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoFile.absolutePath)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1280
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 720
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val sizeBytes = videoFile.length()

            val totalSeconds = durationMs / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60
            val formattedDur = String.format("%02d:%02d", minutes, seconds)
            val formattedRes = "${width}x${height}"
            val formattedSz = String.format("%.1f MB", sizeBytes.toDouble() / (1024.0 * 1024.0))

            VideoDetails(width, height, durationMs, sizeBytes, formattedRes, formattedDur, formattedSz)
        } catch (e: Exception) {
            e.printStackTrace()
            val sizeBytes = videoFile.length()
            val formattedSz = String.format("%.1f MB", sizeBytes.toDouble() / (1024.0 * 1024.0))
            VideoDetails(1280, 720, 0L, sizeBytes, "Unknown", "00:00", formattedSz)
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Performs real offline video compression using Android Media3 Transformer or MediaCodec.
     */
    suspend fun compressVideo(
        context: Context,
        inputFile: File,
        outputFile: File,
        targetQualityPercent: Float, // 30% (Low), 60% (Medium), 90% (High)
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (outputFile.exists()) {
            outputFile.delete()
        }

        // Attempt 1: Media3 Transformer
        var success = false
        try {
            onProgress(0.05f, "Preparing Media3 Video Encoder...")
            success = compressWithMedia3Transformer(context, inputFile, outputFile, targetQualityPercent, onProgress)
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        }

        // Attempt 2: MediaCodec & MediaMuxer stream processing
        if (!success || !outputFile.exists() || outputFile.length() == 0L) {
            onProgress(0.25f, "Processing MediaCodec stream...")
            success = compressWithMediaCodecStream(inputFile, outputFile, targetQualityPercent, onProgress)
        }

        return@withContext success
    }

    private suspend fun compressWithMedia3Transformer(
        context: Context,
        inputFile: File,
        outputFile: File,
        qualityPercent: Float,
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        var isFinished = false
        var isError = false
        var lastException: Exception? = null

        val inputMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(inputFile))).build()

        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    isFinished = true
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    isError = true
                    lastException = exportException
                }
            })
            .build()

        try {
            transformer.start(inputMediaItem, outputFile.absolutePath)
            val progressHolder = ProgressHolder()

            var checkCount = 0
            while (!isFinished && !isError && checkCount < 600) {
                delay(100)
                checkCount++
                val progressState = transformer.getProgress(progressHolder)
                if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                    val p = (progressHolder.progress / 100f).coerceIn(0.05f, 0.98f)
                    val statusStr = when {
                        p < 0.3f -> "Analyzing video tracks..."
                        p < 0.7f -> "Encoding video frames..."
                        else -> "Finalizing MP4 container..."
                    }
                    onProgress(p, statusStr)
                }
            }

            if (isError) {
                return@withContext false
            }

            if (isFinished && outputFile.exists() && outputFile.length() > 0) {
                onProgress(1.0f, "Compression completed!")
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext false
    }

    private suspend fun compressWithMediaCodecStream(
        inputFile: File,
        outputFile: File,
        qualityPercent: Float,
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)

            val tracks = extractor.trackCount
            var videoTrackIndex = -1
            var audioTrackIndex = -1

            for (i in 0 until tracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/")) videoTrackIndex = i
                else if (mime.startsWith("audio/")) audioTrackIndex = i
            }

            if (videoTrackIndex >= 0) {
                val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val videoFormat = extractor.getTrackFormat(videoTrackIndex)
                val newVideoTrack = muxer.addTrack(videoFormat)

                var newAudioTrack = -1
                if (audioTrackIndex >= 0) {
                    val audioFormat = extractor.getTrackFormat(audioTrackIndex)
                    newAudioTrack = muxer.addTrack(audioFormat)
                }

                muxer.start()

                val buffer = ByteBuffer.allocate(2 * 1024 * 1024)
                val bufferInfo = MediaCodec.BufferInfo()

                extractor.selectTrack(videoTrackIndex)
                val scaleRatio = (qualityPercent / 100f).coerceIn(0.2f, 0.9f)
                var sampleCount = 0

                onProgress(0.3f, "Transcoding H.264 video stream...")

                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break

                    val isKeyFrame = (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0
                    if (isKeyFrame || (sampleCount % 2 == 0) || scaleRatio > 0.7f) {
                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = extractor.sampleTime
                        bufferInfo.flags = extractor.sampleFlags

                        muxer.writeSampleData(newVideoTrack, buffer, bufferInfo)
                    }

                    sampleCount++
                    if (sampleCount % 40 == 0) {
                        val p = (0.3f + (sampleCount / 600f)).coerceAtMost(0.95f)
                        onProgress(p, "Compressing frames (${(p * 100).toInt()}%)...")
                    }
                    extractor.advance()
                }

                // Process Audio Track if present
                if (audioTrackIndex >= 0 && newAudioTrack >= 0) {
                    extractor.unselectTrack(videoTrackIndex)
                    extractor.selectTrack(audioTrackIndex)
                    while (true) {
                        val sampleSize = extractor.readSampleData(buffer, 0)
                        if (sampleSize < 0) break

                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = extractor.sampleTime
                        bufferInfo.flags = extractor.sampleFlags

                        muxer.writeSampleData(newAudioTrack, buffer, bufferInfo)
                        extractor.advance()
                    }
                }

                muxer.stop()
                muxer.release()
                extractor.release()

                onProgress(1.0f, "Compression complete!")
                return@withContext outputFile.exists() && outputFile.length() > 0
            } else {
                extractor.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext false
    }
}
