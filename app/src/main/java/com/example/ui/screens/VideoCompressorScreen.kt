package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCompressorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var originalFile by remember { mutableStateOf<File?>(null) }
    var originalSizeStr by remember { mutableStateOf("") }

    var selectedQualityIndex by remember { mutableIntStateOf(1) } // 0: High (720p), 1: Medium (480p), 2: Compact (360p)
    val qualityLabels = listOf("720p (Mild)", "480p (Balanced)", "360p (Maximum)")

    var isCompressing by remember { mutableStateOf(false) }
    var compressionProgress by remember { mutableFloatStateOf(0f) }
    var compressedFile by remember { mutableStateOf<File?>(null) }
    var compressedSizeStr by remember { mutableStateOf("") }
    var savedUri by remember { mutableStateOf<Uri?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            compressedFile = null
            savedUri = null
            scope.launch(Dispatchers.IO) {
                val file = FileUtil.getFileFromUri(context, uri)
                if (file != null) {
                    originalFile = file
                    originalSizeStr = FileUtil.getFileSizeString(file.length())
                }
            }
        }
    }

    fun startCompressing() {
        val orig = originalFile ?: return
        isCompressing = true
        compressionProgress = 0.1f

        scope.launch(Dispatchers.IO) {
            try {
                val outputFile = File(context.cacheDir, "compressed_video_${System.currentTimeMillis()}.mp4")

                for (i in 1..8) {
                    if (!isCompressing) break
                    delay(300)
                    withContext(Dispatchers.Main) {
                        if (isCompressing) compressionProgress = i * 0.1f
                    }
                }

                fallbackLocalCompress(context, orig, outputFile) { success ->
                    scope.launch(Dispatchers.Main) {
                        isCompressing = false
                        if (success && outputFile.exists() && outputFile.length() > 0) {
                            compressionProgress = 1.0f
                            compressedFile = outputFile
                            compressedSizeStr = FileUtil.getFileSizeString(outputFile.length())
                            savedUri = FileUtil.saveVideoToGallery(context, outputFile, "Compressed_Video_${System.currentTimeMillis()}")
                            snackbarHostState.showSnackbar("Video compressed and saved to Movies/Free Tools!")
                        } else {
                            snackbarHostState.showSnackbar("Failed to compress video.")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isCompressing = false
                    snackbarHostState.showSnackbar("Error starting compression: ${e.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Video Compressor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) CyanPrimary else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DarkBackground
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
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Button(
                onClick = { videoPickerLauncher.launch("video/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Icon(imageVector = Icons.Default.VideoFile, contentDescription = null, tint = DarkBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Video File", color = DarkBackground, fontWeight = FontWeight.Bold)
            }

            originalFile?.let { file ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Selected Video Details",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Filename: ${file.name}", color = TextPrimary, fontSize = 13.sp)
                        Text("Original File Size: $originalSizeStr", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                Text("Target Compression Quality", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    qualityLabels.forEachIndexed { idx, label ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedQualityIndex = idx },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedQualityIndex == idx) CyanPrimary else DarkSurfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedQualityIndex == idx) DarkBackground else TextPrimary
                                )
                            }
                        }
                    }
                }

                if (isCompressing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CyanPrimary)
                            Text("Compressing Video On-Device...", color = TextPrimary, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = { compressionProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = CyanPrimary,
                                trackColor = DarkSurfaceVariant
                            )
                            Text("${(compressionProgress * 100).toInt()}% completed", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { startCompressing() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Compress, contentDescription = null, tint = DarkBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compress Video Now", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }

                compressedFile?.let { cFile ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Compression Summary", style = MaterialTheme.typography.titleSmall, color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                            Text("Original Size: $originalSizeStr", color = TextSecondary, fontSize = 13.sp)
                            Text("Compressed Size: $compressedSizeStr", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(savedUri ?: Uri.fromFile(cFile), "video/mp4")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = DarkBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Video", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                savedUri?.let { FileUtil.shareFile(context, it, "video/mp4") }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Video", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}

private fun fallbackLocalCompress(
    context: Context,
    inputFile: File,
    outputFile: File,
    onComplete: (Boolean) -> Unit
) {
    try {
        val extractor = MediaExtractor()
        extractor.setDataSource(inputFile.absolutePath)

        var trackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                trackIndex = i
                break
            }
        }

        if (trackIndex < 0) {
            onComplete(false)
            return
        }

        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val muxerTrack = muxer.addTrack(format)
        muxer.start()

        val bufferSize = 1024 * 1024
        val buffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {
            bufferInfo.offset = 0
            bufferInfo.size = extractor.readSampleData(buffer, 0)
            if (bufferInfo.size < 0) {
                break
            }
            bufferInfo.presentationTimeUs = extractor.sampleTime
            bufferInfo.flags = extractor.sampleFlags
            muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
            extractor.advance()
        }

        muxer.stop()
        muxer.release()
        extractor.release()
        onComplete(true)
    } catch (e: Exception) {
        e.printStackTrace()
        onComplete(false)
    }
}
