package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.OptIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FileUtil
import com.example.util.VideoTranscoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// Design Theme Colors matching the visual screenshot reference
private val OrangePrimary = Color(0xFFFF8C00) // Vibrant Orange
private val OrangeAmber = Color(0xFFF59E0B)   // Warm Amber
private val OrangeDarkBg = Color(0xFF0D0B18)  // Midnight Deep Dark
private val SurfaceCardBg = Color(0xFF161329) // Rich Dark Surface
private val CardBorderColor = Color(0xFF282240)// Subtle Border
private val DashedBorderColor = Color(0xFF3F3763) // Dashed Border
private val GreenSavedText = Color(0xFF10B981) // Emerald Green

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoCompressorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Selected Original Video State
    var selectedVideoFile by remember { mutableStateOf<File?>(null) }
    var videoTitle by remember { mutableStateOf("") }
    var videoResolution by remember { mutableStateOf("") }
    var videoDuration by remember { mutableStateOf("") }
    var originalSizeBytes by remember { mutableStateOf(0L) }
    var originalSizeMb by remember { mutableStateOf(0.0) }
    var videoThumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Compression Settings State
    var compressionRatioPercent by remember { mutableFloatStateOf(60f) } // 30% Low, 60% Medium, 90% High
    var qualityMenuExpanded by remember { mutableStateOf(false) }

    // Compression Execution & Progress State
    var isCompressing by remember { mutableStateOf(false) }
    var compressionProgress by remember { mutableFloatStateOf(0f) }
    var compressionStatusText by remember { mutableStateOf("Preparing compressor engine...") }
    var isCompressionDone by remember { mutableStateOf(false) }

    // Compressed Video Output State
    var compressedTitle by remember { mutableStateOf("") }
    var compressedSizeBytes by remember { mutableStateOf(0L) }
    var compressedSizeMb by remember { mutableStateOf(0.0) }
    var compressedFile by remember { mutableStateOf<File?>(null) }
    var savedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Media3 ExoPlayer Dialog State
    var showPlayerDialog by remember { mutableStateOf(false) }
    var playingFile by remember { mutableStateOf<File?>(null) }
    var playingTitle by remember { mutableStateOf("") }

    // Real Estimated Size Calculation based on original size and quality ratio
    val estimatedSizeMb = remember(originalSizeMb, compressionRatioPercent) {
        if (originalSizeMb <= 0.0) 0.0
        else {
            val estimated = originalSizeMb * (1f - (compressionRatioPercent / 100f) * 0.65f)
            Math.max(0.1, estimated)
        }
    }
    val estimatedSavedPercentage = remember(originalSizeMb, estimatedSizeMb) {
        if (originalSizeMb <= 0.0) 0
        else {
            val pct = ((originalSizeMb - estimatedSizeMb) / originalSizeMb * 100).toInt()
            Math.max(1, pct)
        }
    }

    // Gallery Video Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val file = FileUtil.getFileFromUri(context, uri)
                if (file != null && file.exists()) {
                    val details = VideoTranscoder.getVideoDetails(context, file)
                    val name = file.name.takeIf { it.isNotBlank() && it != "temp_media_.tmp" } ?: "Video_${System.currentTimeMillis()}.mp4"

                    // Extract frame thumbnail
                    var thumbnail: Bitmap? = null
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(file.absolutePath)
                        thumbnail = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try { retriever.release() } catch (_: Exception) {}
                    }

                    val sizeMb = file.length().toDouble() / (1024.0 * 1024.0)

                    withContext(Dispatchers.Main) {
                        selectedVideoFile = file
                        videoTitle = name
                        videoResolution = details.formattedResolution
                        videoDuration = details.formattedDuration
                        originalSizeBytes = file.length()
                        originalSizeMb = sizeMb
                        videoThumbnailBitmap = thumbnail
                        isCompressionDone = false
                        compressedFile = null
                        savedVideoUri = null

                        snackbarHostState.showSnackbar("Video loaded: $name (${details.formattedSize})")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Unable to access selected video file.")
                    }
                }
            }
        }
    }

    // Real Compression Process Launcher
    fun startRealCompression() {
        val input = selectedVideoFile
        if (input == null || !input.exists()) {
            scope.launch { snackbarHostState.showSnackbar("Please select a video file first.") }
            return
        }

        isCompressing = true
        compressionProgress = 0.05f
        isCompressionDone = false

        scope.launch(Dispatchers.IO) {
            val outputName = if (videoTitle.contains(".")) {
                "${videoTitle.substringBeforeLast(".")}_Compressed.mp4"
            } else {
                "${videoTitle}_Compressed.mp4"
            }
            val outputFile = File(context.cacheDir, outputName)

            val success = VideoTranscoder.compressVideo(
                context = context,
                inputFile = input,
                outputFile = outputFile,
                targetQualityPercent = compressionRatioPercent,
                onProgress = { progress, status ->
                    scope.launch(Dispatchers.Main) {
                        compressionProgress = progress
                        compressionStatusText = status
                    }
                }
            )

            if (success && outputFile.exists()) {
                val actualBytes = outputFile.length()
                val actualMb = actualBytes.toDouble() / (1024.0 * 1024.0)

                // Save compressed video directly to Movies/MuftTools directory
                val savedUri = FileUtil.saveVideoToGallery(context, outputFile, outputName)

                withContext(Dispatchers.Main) {
                    isCompressing = false
                    isCompressionDone = true
                    compressedTitle = outputName
                    compressedSizeBytes = actualBytes
                    compressedSizeMb = actualMb
                    compressedFile = outputFile
                    savedVideoUri = savedUri ?: Uri.fromFile(outputFile)

                    snackbarHostState.showSnackbar("Compression complete! Saved to Movies/MuftTools")
                }
            } else {
                withContext(Dispatchers.Main) {
                    isCompressing = false
                    snackbarHostState.showSnackbar("Failed to compress video. Please try another quality preset.")
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
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Original Video Card (Matching UI Layout Image)
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

                        if (selectedVideoFile == null) {
                            // Empty State with Dashed Border matching screenshot
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .drawDashedBorder(color = DashedBorderColor, radius = 12.dp)
                                    .clickable { videoPickerLauncher.launch("video/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF221D3D)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VideoLibrary,
                                            contentDescription = null,
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Text(
                                        text = "Select a video to compress",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "MP4, MOV, AVI and more",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            // Loaded Video Details Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { videoPickerLauncher.launch("video/*") },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF201B3B))
                                        .clickable {
                                            playingFile = selectedVideoFile
                                            playingTitle = videoTitle
                                            showPlayerDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (videoThumbnailBitmap != null) {
                                        Image(
                                            bitmap = videoThumbnailBitmap!!.asImageBitmap(),
                                            contentDescription = "Thumbnail",
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

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
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

                                Text(
                                    text = "Change",
                                    color = OrangePrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { videoPickerLauncher.launch("video/*") }
                                )
                            }
                        }
                    }
                }

                // Down Arrow Indicator linking cards
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

                // 2. Compressed Video (Preview) Card
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
                                text = "Compressed Video (Preview)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            if (isCompressionDone) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Saved",
                                        tint = GreenSavedText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Saved in Movies/MuftTools",
                                        color = GreenSavedText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (!isCompressionDone) {
                            // Empty Preview Dashed State
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .drawDashedBorder(color = DashedBorderColor, radius = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF221D3D)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = "Preview will appear here",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            // Compressed Result Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF201B3B))
                                        .clickable {
                                            playingFile = compressedFile
                                            playingTitle = compressedTitle
                                            showPlayerDialog = true
                                        },
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
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = compressedTitle,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$videoResolution  |  ${String.format("%.1f MB", compressedSizeMb)}  |  $videoDuration",
                                        color = GreenSavedText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Play and Share Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        playingFile = compressedFile
                                        playingTitle = compressedTitle
                                        showPlayerDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenSavedText),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = OrangeDarkBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Video", color = OrangeDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        savedVideoUri?.let { uri ->
                                            FileUtil.shareFile(context, uri, "video/mp4", "Share Compressed Video")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAmber),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = OrangeDarkBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Video", color = OrangeDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 3. Compression Settings Card
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

                        // Quality Row & Dropdown Menu
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
                                            isCompressionDone = false
                                            qualityMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Medium (60%) - Recommended", color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            compressionRatioPercent = 60f
                                            isCompressionDone = false
                                            qualityMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("High (90%) - Best Quality", color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            compressionRatioPercent = 90f
                                            isCompressionDone = false
                                            qualityMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Orange Quality Slider
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

                        // Slider Quick Preset Labels
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

                        // Estimated Output Size Row
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

                            if (selectedVideoFile == null) {
                                Text(
                                    text = "-- MB (--% Smaller)",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "~${String.format("%.1f MB", estimatedSizeMb)} ($estimatedSavedPercentage% Smaller)",
                                    color = GreenSavedText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Progress Indicator during active encoding
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

                // 4. Compress Video Action Button
                if (!isCompressing) {
                    Button(
                        onClick = {
                            if (selectedVideoFile == null) {
                                videoPickerLauncher.launch("video/*")
                            } else {
                                startRealCompression()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary
                        ),
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
                                text = if (selectedVideoFile == null) "Select Video to Compress" else if (isCompressionDone) "Compress Again" else "Compress Video",
                                color = OrangeDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Features Grid (Matching Image Footer Highlights)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FeatureBadgeItem(icon = Icons.Default.Movie, title = "Supports All\nFormats")
                    FeatureBadgeItem(icon = Icons.Default.Smartphone, title = "Works on All\nDevices")
                    FeatureBadgeItem(icon = Icons.Default.Security, title = "Safe & Secure\nProcessing")
                    FeatureBadgeItem(icon = Icons.Default.AccessTime, title = "Save Time &\nStorage")
                }
            }
        }
    }

    // Media3 ExoPlayer Video Playback Dialog
    if (showPlayerDialog && playingFile != null && playingFile!!.exists()) {
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            confirmButton = {
                TextButton(onClick = { showPlayerDialog = false }) {
                    Text("Close", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = playingTitle,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    ExoPlayerVideoView(
                        videoUri = Uri.fromFile(playingFile),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            containerColor = SurfaceCardBg
        )
    }
}

@Composable
private fun FeatureBadgeItem(icon: ImageVector, title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E1A36))
                .border(1.dp, CardBorderColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrangeAmber,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            color = TextMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}

// Media3 ExoPlayer View Component
@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerVideoView(
    videoUri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(videoUri) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = modifier
    )
}

// Extension to draw dashed border for empty video cards
private fun Modifier.drawDashedBorder(color: Color, radius: androidx.compose.ui.unit.Dp): Modifier = drawWithContent {
    drawContent()
    val strokeWidth = 2.dp.toPx()
    val cornerRadius = radius.toPx()
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth, pathEffect = pathEffect),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
}
