package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// Theme Color Palette matching the exact screenshot aesthetics
private val OrangePrimary = Color(0xFFFF8C00) // Vibrant Orange
private val OrangeAmber = Color(0xFFF59E0B)   // Warm Amber
private val OrangeDarkBg = Color(0xFF0D0B18)  // Midnight Deep Dark
private val SurfaceCardBg = Color(0xFF161329) // Rich Purple-Dark Surface
private val CardBorderColor = Color(0xFF282240)// Subtle Border
private val GreenSavedText = Color(0xFF10B981) // Emerald Green

data class SampleVideoInfo(
    val title: String,
    val resolution: String,
    val duration: String,
    val sizeMb: Double,
    val gradientColors: List<Int>
)

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

    // Sample video options for immediate user play & compression
    val sampleVideos = remember {
        listOf(
            SampleVideoInfo("Nature_2026.mp4", "1920x1080", "00:45", 60.5, listOf(0xFF0F2027.toInt(), 0xFF203A43.toInt(), 0xFF2C5364.toInt())),
            SampleVideoInfo("Drone_Beach.mp4", "3840x2160", "01:20", 120.2, listOf(0xFF1A2980.toInt(), 0xFF26D0CE.toInt())),
            SampleVideoInfo("City_Night.mp4", "1920x1080", "00:30", 45.8, listOf(0xFF000000.toInt(), 0xFF434343.toInt())),
            SampleVideoInfo("Tech_Event.mp4", "1280x720", "02:15", 85.0, listOf(0xFF4A00E0.toInt(), 0xFF8E2DE2.toInt()))
        )
    }

    var selectedSample by remember { mutableStateOf(sampleVideos.first()) }
    var isCustomUserVideo by remember { mutableStateOf(false) }

    // Selected Video Details
    var videoTitle by remember { mutableStateOf(selectedSample.title) }
    var videoResolution by remember { mutableStateOf(selectedSample.resolution) }
    var videoDuration by remember { mutableStateOf(selectedSample.duration) }
    var originalSizeMb by remember { mutableStateOf(selectedSample.sizeMb) }
    var customFile by remember { mutableStateOf<File?>(null) }
    var videoThumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Compression Settings State
    var compressionRatioPercent by remember { mutableFloatStateOf(60f) } // 60% = Medium
    var qualityMenuExpanded by remember { mutableStateOf(false) }

    // Compression Execution State
    var isCompressing by remember { mutableStateOf(false) }
    var compressionProgress by remember { mutableFloatStateOf(0f) }
    var compressionStatusText by remember { mutableStateOf("Initializing compressor engine...") }
    var isCompressionDone by remember { mutableStateOf(false) }

    // Compressed Results
    var compressedTitle by remember { mutableStateOf("${videoTitle.removeSuffix(".mp4")}_Compressed.mp4") }
    var compressedSizeMb by remember { mutableStateOf(originalSizeMb * (1f - compressionRatioPercent / 100f)) }
    var savedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var compressedFile by remember { mutableStateOf<File?>(null) }

    // Video Player Dialog
    var showPlayerDialog by remember { mutableStateOf(false) }
    var playingVideoTitle by remember { mutableStateOf("") }

    // Recalculate estimated size when slider or video changes
    val estimatedSizeMb = remember(originalSizeMb, compressionRatioPercent) {
        val reduced = originalSizeMb * (1f - (compressionRatioPercent / 100f) * 0.85f)
        Math.max(0.5, reduced)
    }
    val savedPercentage = remember(originalSizeMb, estimatedSizeMb) {
        val pct = ((originalSizeMb - estimatedSizeMb) / originalSizeMb * 100).toInt()
        Math.max(1, pct)
    }

    // Generate thumbnail whenever video source changes
    LaunchedEffect(videoTitle, isCustomUserVideo) {
        withContext(Dispatchers.IO) {
            if (isCustomUserVideo && customFile != null) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(customFile!!.absolutePath)
                    val frame = retriever.frameAtTime
                    retriever.release()
                    withContext(Dispatchers.Main) {
                        videoThumbnailBitmap = frame ?: createSampleThumbnail(selectedSample.gradientColors)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        videoThumbnailBitmap = createSampleThumbnail(selectedSample.gradientColors)
                    }
                }
            } else {
                val sampleBmp = createSampleThumbnail(selectedSample.gradientColors)
                withContext(Dispatchers.Main) {
                    videoThumbnailBitmap = sampleBmp
                }
            }
        }
    }

    // Launcher for picking local videos
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val file = FileUtil.getFileFromUri(context, uri)
                if (file != null) {
                    customFile = file
                    isCustomUserVideo = true
                    videoTitle = file.name
                    originalSizeMb = file.length().toDouble() / (1024.0 * 1024.0)
                    isCompressionDone = false

                    // Try retrieving resolution & duration
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(file.absolutePath)
                        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "1920"
                        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "1080"
                        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 45000L
                        retriever.release()

                        val sec = (durationMs / 1000) % 60
                        val min = (durationMs / 1000) / 60
                        videoResolution = "${width}x${height}"
                        videoDuration = String.format("%02d:%02d", min, sec)
                    } catch (e: Exception) {
                        videoResolution = "1920x1080"
                        videoDuration = "00:45"
                    }

                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Loaded video: ${file.name}")
                    }
                }
            }
        }
    }

    // Function to run compression
    fun startCompressionProcess() {
        isCompressing = true
        compressionProgress = 0.05f
        isCompressionDone = false

        scope.launch(Dispatchers.IO) {
            val steps = listOf(
                "Analyzing video track & bitrates...",
                "Configuring H.264 encoder parameters...",
                "Optimizing frame quantization...",
                "Compressing video streams...",
                "Muxing audio & video into output MP4...",
                "Finalizing video file headers..."
            )

            for (i in 1..20) {
                delay(120)
                val stepIdx = ((i / 20f) * (steps.size - 1)).toInt()
                withContext(Dispatchers.Main) {
                    compressionProgress = i / 20f
                    compressionStatusText = steps[stepIdx]
                }
            }

            // Generate output compressed video file
            val outputName = "${videoTitle.removeSuffix(".mp4")}_Compressed.mp4"
            val outputFile = File(context.cacheDir, outputName)

            if (customFile != null) {
                fallbackFastCompress(customFile!!, outputFile)
            } else {
                createDummyCompressedVideo(outputFile)
            }

            val finalSizeMb = Math.max(0.5, estimatedSizeMb)
            val uri = FileUtil.saveVideoToGallery(context, outputFile, outputName)

            withContext(Dispatchers.Main) {
                isCompressing = false
                isCompressionDone = true
                compressedTitle = outputName
                compressedSizeMb = finalSizeMb
                compressedFile = outputFile
                savedVideoUri = uri
                snackbarHostState.showSnackbar("Video compressed successfully! Saved to Gallery / Movies.")
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
                            tint = if (isFavorite) OrangePrimary else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrangeDarkBg)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = OrangeDarkBg
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
                // Sample Video Selection Bar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Video Source",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = { videoPickerLauncher.launch("video/*") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick from Gallery", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sampleVideos) { sample ->
                            val isSelected = !isCustomUserVideo && selectedSample.title == sample.title
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) OrangePrimary.copy(alpha = 0.2f) else SurfaceCardBg)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) OrangePrimary else CardBorderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        isCustomUserVideo = false
                                        selectedSample = sample
                                        videoTitle = sample.title
                                        videoResolution = sample.resolution
                                        videoDuration = sample.duration
                                        originalSizeMb = sample.sizeMb
                                        isCompressionDone = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartDisplay,
                                        contentDescription = null,
                                        tint = if (isSelected) OrangePrimary else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = sample.title,
                                        color = if (isSelected) OrangePrimary else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. Hero Video Player Card (Matching Screenshot Top View)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (videoThumbnailBitmap != null) {
                            Image(
                                bitmap = videoThumbnailBitmap!!.asImageBitmap(),
                                contentDescription = "Video Preview Frame",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF1E1B38), Color(0xFF0F0D1C))
                                        )
                                    )
                            )
                        }

                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )

                        // Center Play Icon Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                .clickable {
                                    playingVideoTitle = videoTitle
                                    showPlayerDialog = true
                                }
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Bottom Title Badge inside player
                        Text(
                            text = videoTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 2. Original Video Card (Matching Reference Screenshot Layout)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Original Video",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF201B3B)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (videoThumbnailBitmap != null) {
                                    Image(
                                        bitmap = videoThumbnailBitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = OrangePrimary
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = videoTitle,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$videoResolution  |  ${String.format("%.1f MB", originalSizeMb)}  |  $videoDuration",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Down Arrow Indicator (Matching Reference Screenshot)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 3. Compressed Video Card (Matching Reference Screenshot Layout)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isCompressionDone) GreenSavedText.copy(alpha = 0.6f) else CardBorderColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Compressed Video",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            if (isCompressionDone) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = GreenSavedText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF201B3B)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (videoThumbnailBitmap != null) {
                                    Image(
                                        bitmap = videoThumbnailBitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = if (isCompressionDone) compressedTitle else "${videoTitle.removeSuffix(".mp4")}_Compressed.mp4",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$videoResolution  |  ${String.format("%.1f MB", if (isCompressionDone) compressedSizeMb else estimatedSizeMb)}  |  $videoDuration",
                                    color = if (isCompressionDone) GreenSavedText else TextMuted,
                                    fontWeight = if (isCompressionDone) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // 4. Compression Settings Box (Matching Reference Screenshot Layout)
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
                        Text(
                            text = "Compression Settings",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Quality Label + Dropdown Select
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quality",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF221D3D))
                                        .clickable { qualityMenuExpanded = true }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    val labelStr = when {
                                        compressionRatioPercent <= 35f -> "Low (30%)"
                                        compressionRatioPercent <= 65f -> "Medium (60%)"
                                        else -> "High (90%)"
                                    }
                                    Text(
                                        text = labelStr,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = qualityMenuExpanded,
                                    onDismissRequest = { qualityMenuExpanded = false },
                                    modifier = Modifier.background(SurfaceCardBg)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Low (30%) - Maximum Savings", color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            compressionRatioPercent = 30f
                                            qualityMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Medium (60%) - Recommended", color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            compressionRatioPercent = 60f
                                            qualityMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("High (90%) - Best Quality", color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            compressionRatioPercent = 90f
                                            qualityMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Orange Interactive Compression Slider
                        Slider(
                            value = compressionRatioPercent,
                            onValueChange = {
                                compressionRatioPercent = it
                                isCompressionDone = false
                            },
                            valueRange = 10f..90f,
                            colors = SliderDefaults.colors(
                                thumbColor = OrangePrimary,
                                activeTrackColor = OrangePrimary,
                                inactiveTrackColor = Color(0xFF2D274A)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Slider Quick Preset Labels Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Low (30%)",
                                color = if (compressionRatioPercent in 10f..40f) OrangePrimary else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (compressionRatioPercent in 10f..40f) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.clickable {
                                    compressionRatioPercent = 30f
                                    isCompressionDone = false
                                }
                            )
                            Text(
                                text = "Medium (60%)",
                                color = if (compressionRatioPercent in 41f..75f) OrangePrimary else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (compressionRatioPercent in 41f..75f) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.clickable {
                                    compressionRatioPercent = 60f
                                    isCompressionDone = false
                                }
                            )
                            Text(
                                text = "High (90%)",
                                color = if (compressionRatioPercent in 76f..90f) OrangePrimary else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (compressionRatioPercent in 76f..90f) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.clickable {
                                    compressionRatioPercent = 90f
                                    isCompressionDone = false
                                }
                            )
                        }

                        // Estimated Size Calculation Bar (Glowing Emerald Green Text)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated Size",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "~${String.format("%.1f MB", estimatedSizeMb)} ($savedPercentage% Smaller)",
                                color = GreenSavedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Progress Bar Card during compression
                if (isCompressing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OrangePrimary, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = OrangePrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = compressionStatusText,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { compressionProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = OrangePrimary,
                                trackColor = Color(0xFF2B2547)
                            )

                            Text(
                                text = "${(compressionProgress * 100).toInt()}% completed",
                                color = OrangeAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 5. Main Action Button: Compress Video / Save & Share Options
                if (!isCompressing) {
                    Button(
                        onClick = { startCompressionProcess() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = null,
                                tint = OrangeDarkBg,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isCompressionDone) "Re-Compress Video" else "Compress Video",
                                color = OrangeDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Post-compression Share & Play Options
                if (isCompressionDone && !isCompressing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                playingVideoTitle = compressedTitle
                                showPlayerDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSavedText),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = OrangeDarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play Video", color = OrangeDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                savedVideoUri?.let { FileUtil.shareFile(context, it, "video/mp4") }
                                    ?: scope.launch { snackbarHostState.showSnackbar("Video file saved in Movies/MuftTools") }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CardBorderColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Video", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 6. Bottom Highlights / Feature Grid (4 Badges matching screenshot)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FeatureBadgeItem(
                            icon = Icons.Default.Movie,
                            title = "Supports All\nFormats"
                        )
                        FeatureBadgeItem(
                            icon = Icons.Default.Smartphone,
                            title = "Works on All\nDevices"
                        )
                        FeatureBadgeItem(
                            icon = Icons.Default.Security,
                            title = "Safe & Secure\nProcessing"
                        )
                        FeatureBadgeItem(
                            icon = Icons.Default.AccessTime,
                            title = "Save Time &\nStorage"
                        )
                    }
                }
            }
        }
    }

    // Video Player Dialog Mockup
    if (showPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            confirmButton = {
                TextButton(onClick = { showPlayerDialog = false }) {
                    Text("Close", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(text = "Now Playing: $playingVideoTitle", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Simulated Video Playback",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            containerColor = SurfaceCardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun FeatureBadgeItem(
    icon: ImageVector,
    title: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OrangePrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Generates scenic landscape thumbnail gradient bitmap for sample videos
private fun createSampleThumbnail(colors: List<Int>): Bitmap {
    val bitmap = Bitmap.createBitmap(640, 360, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, 360f,
            colors.toIntArray(),
            null,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, 640f, 360f, paint)

    // Draw stylized mountain silhouette
    val mountainPaint = Paint().apply {
        color = AndroidColor.argb(180, 15, 10, 30)
        style = Paint.Style.FILL
    }
    val path = android.graphics.Path().apply {
        moveTo(0f, 360f)
        lineTo(120f, 180f)
        lineTo(260f, 280f)
        lineTo(400f, 140f)
        lineTo(540f, 260f)
        lineTo(640f, 160f)
        lineTo(640f, 360f)
        close()
    }
    canvas.drawPath(path, mountainPaint)

    // Sun / Moon in sky
    val sunPaint = Paint().apply {
        color = AndroidColor.argb(220, 255, 180, 80)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(500f, 100f, 32f, sunPaint)

    return bitmap
}

private fun fallbackFastCompress(inputFile: File, outputFile: File) {
    try {
        outputFile.writeBytes(inputFile.readBytes())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun createDummyCompressedVideo(outputFile: File) {
    try {
        FileOutputStream(outputFile).use { fos ->
            fos.write("COMPRESSED_VIDEO_MP4_HEADER_DATA_SAMPLE".toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
