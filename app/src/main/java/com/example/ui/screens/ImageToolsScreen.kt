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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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

    // Crop State
    var selectedCropRatio by remember { mutableStateOf("Square (1:1)") }

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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavorite() }) {
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
                                                val uri = FileUtil.createTempImageUri(context)
                                                tempCameraUri = uri
                                                cameraLauncher.launch(uri)
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
                        onClick = { activeToolMode = ToolMode.CROP },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.AspectRatio,
                        label = "Resize",
                        isSelected = activeToolMode == ToolMode.RESIZE,
                        onClick = { activeToolMode = ToolMode.RESIZE },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.Download,
                        label = "Compress",
                        isSelected = activeToolMode == ToolMode.COMPRESS,
                        onClick = { activeToolMode = ToolMode.COMPRESS },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.Refresh,
                        label = "Convert",
                        isSelected = activeToolMode == ToolMode.CONVERT,
                        onClick = { activeToolMode = ToolMode.CONVERT },
                        modifier = Modifier.weight(1f)
                    )
                    ToolModeItem(
                        icon = Icons.Default.RotateRight,
                        label = "Rotate",
                        isSelected = activeToolMode == ToolMode.ROTATE,
                        onClick = { activeToolMode = ToolMode.ROTATE },
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
                                Text("Crop Aspect Ratio Presets", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                val ratios = listOf("Square (1:1)", "Landscape (16:9)", "Portrait (4:3)", "Full (9:16)")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ratios.forEach { ratioLabel ->
                                        val isSel = selectedCropRatio == ratioLabel
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) PurplePrimary else Color(0xFF201A38))
                                                .clickable {
                                                    selectedCropRatio = ratioLabel
                                                    originalBitmap?.let { bmp ->
                                                        val w = bmp.width
                                                        val h = bmp.height
                                                        val cropped = when (ratioLabel) {
                                                            "Square (1:1)" -> {
                                                                val sz = Math.min(w, h)
                                                                Bitmap.createBitmap(bmp, (w - sz) / 2, (h - sz) / 2, sz, sz)
                                                            }
                                                            "Landscape (16:9)" -> {
                                                                val targetH = (w * 9 / 16).coerceAtMost(h)
                                                                Bitmap.createBitmap(bmp, 0, (h - targetH) / 2, w, targetH)
                                                            }
                                                            "Portrait (4:3)" -> {
                                                                val targetW = (h * 4 / 3).coerceAtMost(w)
                                                                Bitmap.createBitmap(bmp, (w - targetW) / 2, 0, targetW, h)
                                                            }
                                                            else -> { // 9:16
                                                                val targetW = (h * 9 / 16).coerceAtMost(w)
                                                                Bitmap.createBitmap(bmp, (w - targetW) / 2, 0, targetW, h)
                                                            }
                                                        }
                                                        editedBitmap = cropped
                                                    }
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                ratioLabel.substringBefore(" "),
                                                color = if (isSel) Color.White else TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
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
                                                    originalBitmap?.let { bmp ->
                                                        val nw = (bmp.width * pct / 100).coerceAtLeast(10)
                                                        val nh = (bmp.height * pct / 100).coerceAtLeast(10)
                                                        editedBitmap = Bitmap.createScaledBitmap(bmp, nw, nh, true)
                                                        targetWidthStr = nw.toString()
                                                        targetHeightStr = nh.toString()
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
                                            if (w != null && w > 0 && originalBitmap != null) {
                                                val h = if (maintainAspect) (w * originalBitmap!!.height / originalBitmap!!.width) else targetHeightStr.toIntOrNull() ?: originalBitmap!!.height
                                                targetHeightStr = h.toString()
                                                editedBitmap = Bitmap.createScaledBitmap(originalBitmap!!, w, h, true)
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
                                            if (h != null && h > 0 && originalBitmap != null) {
                                                val w = if (maintainAspect) (h * originalBitmap!!.width / originalBitmap!!.height) else targetWidthStr.toIntOrNull() ?: originalBitmap!!.width
                                                targetWidthStr = w.toString()
                                                editedBitmap = Bitmap.createScaledBitmap(originalBitmap!!, w, h, true)
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
                                                .clickable { targetFormat = fmt }
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
                                            editedBitmap?.let { bmp ->
                                                val matrix = Matrix().apply { postRotate(90f) }
                                                editedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
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
                                            editedBitmap?.let { bmp ->
                                                val matrix = Matrix().apply { postScale(-1f, 1f) }
                                                editedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
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
                                            editedBitmap?.let { bmp ->
                                                val matrix = Matrix().apply { postScale(1f, -1f) }
                                                editedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
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

