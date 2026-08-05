package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.db.AppDatabase
import com.example.data.db.QrCodeEntity
import com.example.ui.components.CameraPreview
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FileUtil
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

// Color Palette specifically tailored for QR Scanner & Generator matching image
private val GreenNeon = Color(0xFF00E676)
private val GreenEmerald = Color(0xFF10B981)
private val GreenDarkBg = Color(0xFF0D0B18)
private val SurfaceCardBg = Color(0xFF161329)
private val CardBorderColor = Color(0xFF272242)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation bar tab index: 0 = Scanner / Generator, 1 = History, 2 = Settings/Offline
    var bottomNavIndex by remember { mutableIntStateOf(0) }

    // Mode switch for tab index 0: 0 = SCAN, 1 = GENERATE
    var scanModeIndex by remember { mutableIntStateOf(0) }

    var flashEnabled by remember { mutableStateOf(false) }

    val qrDao = remember { AppDatabase.getDatabase(context).qrCodeDao() }
    val historyList by qrDao.getAllQrCodes().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreenNeon.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = GreenNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "QR Scanner & Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
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
                    if (bottomNavIndex == 0 && scanModeIndex == 0) {
                        IconButton(onClick = { flashEnabled = !flashEnabled }) {
                            Icon(
                                imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash Toggle",
                                tint = if (flashEnabled) Color.Yellow else TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) GreenNeon else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenDarkBg)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF100E20),
                contentColor = GreenNeon
            ) {
                NavigationBarItem(
                    selected = bottomNavIndex == 0,
                    onClick = { bottomNavIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner & Generator"
                        )
                    },
                    label = { Text("Generate/Scan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GreenDarkBg,
                        selectedTextColor = GreenNeon,
                        indicatorColor = GreenNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 1,
                    onClick = { bottomNavIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Scan History"
                        )
                    },
                    label = { Text("History", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GreenDarkBg,
                        selectedTextColor = GreenNeon,
                        indicatorColor = GreenNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 2,
                    onClick = { bottomNavIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GreenDarkBg,
                        selectedTextColor = GreenNeon,
                        indicatorColor = GreenNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = GreenDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                when (bottomNavIndex) {
                    0 -> MainScannerGeneratorView(
                        context = context,
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        scanModeIndex = scanModeIndex,
                        onScanModeChanged = { scanModeIndex = it },
                        flashEnabled = flashEnabled,
                        onToggleFlash = { flashEnabled = !flashEnabled },
                        onSaveToHistory = { entity ->
                            scope.launch(Dispatchers.IO) {
                                qrDao.insertQrCode(entity)
                            }
                        }
                    )

                    1 -> QrHistoryView(
                        context = context,
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        historyList = historyList,
                        onDeleteScan = { id ->
                            scope.launch(Dispatchers.IO) {
                                qrDao.deleteQrCode(id)
                            }
                        },
                        onClearAll = {
                            scope.launch(Dispatchers.IO) {
                                qrDao.clearAll()
                            }
                        }
                    )

                    2 -> QrSettingsView(
                        onNavigateToScanner = {
                            bottomNavIndex = 0
                            scanModeIndex = 0
                        },
                        onNavigateToGenerator = {
                            bottomNavIndex = 0
                            scanModeIndex = 1
                        },
                        onNavigateToHistory = {
                            bottomNavIndex = 1
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScannerGeneratorView(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    scanModeIndex: Int,
    onScanModeChanged: (Int) -> Unit,
    flashEnabled: Boolean,
    onToggleFlash: () -> Unit,
    onSaveToHistory: (QrCodeEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Tab Bar: SCAN vs GENERATE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCardBg)
                .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (scanModeIndex == 0) GreenNeon else Color.Transparent)
                    .clickable { onScanModeChanged(0) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = if (scanModeIndex == 0) GreenDarkBg else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SCAN",
                        fontWeight = FontWeight.Bold,
                        color = if (scanModeIndex == 0) GreenDarkBg else TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (scanModeIndex == 1) GreenNeon else Color.Transparent)
                    .clickable { onScanModeChanged(1) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = if (scanModeIndex == 1) GreenDarkBg else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "GENERATE",
                        fontWeight = FontWeight.Bold,
                        color = if (scanModeIndex == 1) GreenDarkBg else TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (scanModeIndex == 0) {
            ScanQrSection(
                context = context,
                scope = scope,
                snackbarHostState = snackbarHostState,
                flashEnabled = flashEnabled,
                onToggleFlash = onToggleFlash,
                onSaveToHistory = onSaveToHistory
            )
        } else {
            GenerateQrSection(
                context = context,
                scope = scope,
                snackbarHostState = snackbarHostState,
                onSaveToHistory = onSaveToHistory
            )
        }
    }
}

@Composable
private fun ScanQrSection(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    flashEnabled: Boolean,
    onToggleFlash: () -> Unit,
    onSaveToHistory: (QrCodeEntity) -> Unit
) {
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var scannedType by remember { mutableStateOf("Website") }
    var isCameraAvailable by remember { mutableStateOf(true) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission required for live QR scanning.")
            }
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val raw = barcodes.first().rawValue ?: ""
                            scannedResult = raw
                            scannedType = detectQrType(raw)
                            scope.launch { snackbarHostState.showSnackbar("QR Code detected!") }
                            onSaveToHistory(
                                QrCodeEntity(
                                    content = raw,
                                    type = scannedType,
                                    isGenerated = false
                                )
                            )
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("No QR code found in selected image.") }
                        }
                    }
                    .addOnFailureListener {
                        scope.launch { snackbarHostState.showSnackbar("Failed to scan image.") }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also { analysis ->
                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    processImageProxy(imageProxy) { result ->
                        if (scannedResult != result) {
                            scannedResult = result
                            val detectedType = detectQrType(result)
                            scannedType = detectedType
                            scope.launch { snackbarHostState.showSnackbar("QR Code Scanned!") }
                            onSaveToHistory(
                                QrCodeEntity(
                                    content = result,
                                    type = detectedType,
                                    isGenerated = false
                                )
                            )
                        }
                    }
                }
            }
    }

    // Camera Scan Box Card
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    imageAnalysis = imageAnalysis,
                    onCameraAvailableChanged = { isCameraAvailable = it }
                )

                // Laser animation inside reticle box
                val infiniteTransition = rememberInfiniteTransition()
                val scanLineY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                // Target Corner Bracket Overlay with glowing scan line
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .align(Alignment.Center)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 6.dp.toPx()
                        val length = 36.dp.toPx()
                        val cornerRadius = 12.dp.toPx()

                        // Top-Left
                        drawPath(
                            path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, length)
                                lineTo(0f, cornerRadius)
                                quadraticTo(0f, 0f, cornerRadius, 0f)
                                lineTo(length, 0f)
                            },
                            color = GreenNeon,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Top-Right
                        drawPath(
                            path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width - length, 0f)
                                lineTo(size.width - cornerRadius, 0f)
                                quadraticTo(size.width, 0f, size.width, cornerRadius)
                                lineTo(size.width, length)
                            },
                            color = GreenNeon,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Bottom-Left
                        drawPath(
                            path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, size.height - length)
                                lineTo(0f, size.height - cornerRadius)
                                quadraticTo(0f, size.height, cornerRadius, size.height)
                                lineTo(length, size.height)
                            },
                            color = GreenNeon,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Bottom-Right
                        drawPath(
                            path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width - length, size.height)
                                lineTo(size.width - cornerRadius, size.height)
                                quadraticTo(size.width, size.height, size.width, size.height - cornerRadius)
                                lineTo(size.width, size.height - length)
                            },
                            color = GreenNeon,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Moving Scan Laser Bar
                        val currentY = size.height * scanLineY
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GreenNeon,
                                    Color.White,
                                    GreenNeon,
                                    Color.Transparent
                                )
                            ),
                            start = Offset(10f, currentY),
                            end = Offset(size.width - 10f, currentY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
            } else {
                // Permission required card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GreenNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = GreenNeon,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Camera Permission Needed",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enable camera permission to scan QR codes live with your camera.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenNeon)
                    ) {
                        Text("Enable Camera", color = GreenDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Action Controls: Gallery Scanner & Sample QR Scanner
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { galleryPickerLauncher.launch("image/*") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardBg),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = GreenNeon)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Gallery Image", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        Button(
            onClick = {
                val sampleUrl = "https://example.com"
                scannedResult = sampleUrl
                scannedType = "Website"
                scope.launch { snackbarHostState.showSnackbar("Sample QR Code scanned successfully!") }
                onSaveToHistory(
                    QrCodeEntity(
                        content = sampleUrl,
                        type = "Website",
                        isGenerated = false
                    )
                )
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = GreenNeon.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = GreenNeon)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Test Sample QR", color = GreenNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }

    // Scanned Result Card (Matching Reference Screenshot Design)
    scannedResult?.let { text ->
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GreenNeon.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GreenNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = GreenNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Scanned Result",
                            style = MaterialTheme.typography.titleSmall,
                            color = GreenNeon,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = scannedType,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F0D1C))
                        .padding(12.dp)
                )

                // 3 Action Buttons: Open, Copy, Share (Matching Reference Screenshot)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            handleOpenAction(context, text, scannedType, scope, snackbarHostState)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenNeon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = GreenDarkBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", color = GreenDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("QR Code", text)
                            clipboard.setPrimaryClip(clip)
                            scope.launch { snackbarHostState.showSnackbar("Copied to clipboard!") }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CardBorderColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share QR Code Content"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CardBorderColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // 100% Offline Badge Box
    OfflineNoticeCard()
}

@Composable
private fun GenerateQrSection(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSaveToHistory: (QrCodeEntity) -> Unit
) {
    var selectedType by remember { mutableStateOf("URL") }

    // State variables for inputs
    var urlText by remember { mutableStateOf("https://example.com") }
    var plainText by remember { mutableStateOf("Hello, World!") }

    // Contact fields
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }

    // Wi-Fi fields
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf("WPA") }

    // More fields (Email / SMS)
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }

    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentEncodedPayload by remember { mutableStateOf("") }

    fun buildPayload(): String {
        return when (selectedType) {
            "URL" -> if (urlText.startsWith("http://") || urlText.startsWith("https://")) urlText else "https://$urlText"
            "Text" -> plainText
            "Contact" -> "BEGIN:VCARD\nVERSION:3.0\nN:$contactName\nTEL:$contactPhone\nEMAIL:$contactEmail\nEND:VCARD"
            "Wi-Fi" -> "WIFI:S:$wifiSsid;T:$wifiSecurity;P:$wifiPassword;;"
            "More" -> "MATMSG:TO:$emailAddress;SUB:$emailSubject;BODY:$emailBody;;"
            else -> urlText
        }
    }

    fun generateQr() {
        val payload = buildPayload()
        if (payload.isBlank()) return
        currentEncodedPayload = payload
        scope.launch(Dispatchers.IO) {
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                    }
                }
                withContext(Dispatchers.Main) {
                    generatedBitmap = bmp
                    onSaveToHistory(
                        QrCodeEntity(
                            content = payload,
                            type = selectedType,
                            isGenerated = true
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Initial default generation on view
    LaunchedEffect(selectedType) {
        generateQr()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Choose Type Header & Categories (Matching Reference Screenshot)
        Text(
            text = "Choose Type",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val typeItems = listOf(
                TypeChipData("URL", Icons.Default.Link),
                TypeChipData("Text", Icons.Default.TextFields),
                TypeChipData("Contact", Icons.Default.Person),
                TypeChipData("Wi-Fi", Icons.Default.Wifi),
                TypeChipData("More", Icons.Default.MoreHoriz)
            )

            typeItems.forEach { item ->
                val isSelected = selectedType == item.name
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) GreenNeon.copy(alpha = 0.15f) else SurfaceCardBg)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) GreenNeon else CardBorderColor,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedType = item.name }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name,
                            tint = if (isSelected) GreenNeon else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GreenNeon else TextSecondary
                        )
                    }
                }
            }
        }

        // Form Fields Container based on Selected Type
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
                    text = "Enter $selectedType Details",
                    color = GreenNeon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                when (selectedType) {
                    "URL" -> {
                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            label = { Text("Enter URL") },
                            placeholder = { Text("https://example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (urlText.isNotEmpty()) {
                                    IconButton(onClick = { urlText = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                    }
                                }
                            },
                            colors = customTextFieldColors()
                        )
                    }

                    "Text" -> {
                        OutlinedTextField(
                            value = plainText,
                            onValueChange = { plainText = it },
                            label = { Text("Enter Text") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = customTextFieldColors()
                        )
                    }

                    "Contact" -> {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                    }

                    "Wi-Fi" -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("Network SSID") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = { wifiPassword = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Security:", color = TextSecondary, fontSize = 12.sp)
                            listOf("WPA", "WEP", "nopass").forEach { sec ->
                                FilterChip(
                                    selected = wifiSecurity == sec,
                                    onClick = { wifiSecurity = sec },
                                    label = { Text(if (sec == "nopass") "Open" else sec) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GreenNeon,
                                        selectedLabelColor = GreenDarkBg
                                    )
                                )
                            }
                        }
                    }

                    "More" -> {
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text("Email Address / Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = { emailSubject = it },
                            label = { Text("Subject / Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = { emailBody = it },
                            label = { Text("Message Body") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = customTextFieldColors()
                        )
                    }
                }

                // Primary Green Generate Button (Matching Reference Design Button)
                Button(
                    onClick = { generateQr() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenNeon),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = GreenDarkBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate QR Code",
                        color = GreenDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Generated QR Code Preview Card
        generatedBitmap?.let { bmp ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val uri = FileUtil.saveBitmapToGallery(context, bmp, "QR_${System.currentTimeMillis()}")
                                if (uri != null) {
                                    scope.launch { snackbarHostState.showSnackbar("QR Code saved to Gallery!") }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenNeon),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = GreenDarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save QR", color = GreenDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val uri = FileUtil.saveBitmapToGallery(context, bmp, "QR_Share")
                                uri?.let { FileUtil.shareFile(context, it, "image/png") }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CardBorderColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share QR", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrHistoryView(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    historyList: List<QrCodeEntity>,
    onDeleteScan: (String) -> Unit,
    onClearAll: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredList = historyList.filter { item ->
        val matchesQuery = searchQuery.isEmpty() || item.content.contains(searchQuery, ignoreCase = true) || item.type.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Scanned" -> !item.isGenerated
            "Generated" -> item.isGenerated
            else -> item.type.equals(selectedFilter, ignoreCase = true)
        }
        matchesQuery && matchesFilter
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all saved QR code history?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearDialog = false
                    scope.launch { snackbarHostState.showSnackbar("History cleared.") }
                }) {
                    Text("Clear", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCardBg
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scan & Generate History",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            if (historyList.isNotEmpty()) {
                TextButton(onClick = { showClearDialog = true }) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", color = Color.Red, fontSize = 12.sp)
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search history...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = customTextFieldColors()
        )

        // Filter Chips Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = listOf("All", "Scanned", "Generated", "URL", "Text", "Contact", "Wi-Fi")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenNeon,
                        selectedLabelColor = GreenDarkBg,
                        containerColor = SurfaceCardBg,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No history items found", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    HistoryItemCard(
                        context = context,
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        item = item,
                        onDelete = { onDeleteScan(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    item: QrCodeEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.isGenerated) GreenEmerald.copy(alpha = 0.2f) else GreenNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isGenerated) Icons.Default.QrCode else Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = if (item.isGenerated) GreenEmerald else GreenNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (item.isGenerated) "Generated (${item.type})" else "Scanned (${item.type})",
                        color = if (item.isGenerated) GreenEmerald else GreenNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = formattedDate,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                text = item.content,
                color = TextPrimary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", item.content))
                        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard!") }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, item.content)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                if (item.content.startsWith("http://") || item.content.startsWith("https://")) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.content))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open", tint = GreenNeon, modifier = Modifier.size(16.dp))
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun QrSettingsView(
    onNavigateToScanner: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "QR Tools Features & Info",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        // Features Grid Cards (Matching Reference Screenshot Left Cards)
        FeatureInfoCard(
            title = "Scan QR Code",
            subtitle = "Instantly scan and read any QR code or barcode offline with live camera.",
            icon = Icons.Default.QrCodeScanner,
            accentColor = GreenNeon,
            onClick = onNavigateToScanner
        )

        FeatureInfoCard(
            title = "Generate QR",
            subtitle = "Create custom QR codes for text, links, contacts, Wi-Fi networks and more.",
            icon = Icons.Default.QrCode,
            accentColor = GreenEmerald,
            onClick = onNavigateToGenerator
        )

        FeatureInfoCard(
            title = "Scan History",
            subtitle = "View, copy, share, and manage all your previously scanned or generated QR codes.",
            icon = Icons.Default.History,
            accentColor = CyanPrimary,
            onClick = onNavigateToHistory
        )

        FeatureInfoCard(
            title = "100% Offline & Private",
            subtitle = "All scanning, barcode detection, and QR code generation work completely offline on your device.",
            icon = Icons.Default.Security,
            accentColor = GreenNeon,
            onClick = {}
        )
    }
}

@Composable
private fun FeatureInfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OfflineNoticeCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GreenNeon.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = GreenNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "100% Offline & Secure",
                    color = GreenNeon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "All QR scanning and generation happen directly on your device.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GreenNeon,
    unfocusedBorderColor = CardBorderColor,
    focusedLabelColor = GreenNeon,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = Color(0xFF0F0D1C),
    unfocusedContainerColor = Color(0xFF0F0D1C)
)

private data class TypeChipData(val name: String, val icon: ImageVector)

private fun detectQrType(content: String): String {
    return when {
        content.startsWith("http://", ignoreCase = true) || content.startsWith("https://", ignoreCase = true) -> "Website"
        content.startsWith("BEGIN:VCARD", ignoreCase = true) -> "Contact"
        content.startsWith("WIFI:", ignoreCase = true) -> "Wi-Fi"
        content.startsWith("mailto:", ignoreCase = true) || content.startsWith("MATMSG:", ignoreCase = true) -> "Email"
        content.startsWith("smsto:", ignoreCase = true) || content.startsWith("sms:", ignoreCase = true) -> "SMS"
        content.startsWith("tel:", ignoreCase = true) -> "Phone"
        else -> "Text"
    }
}

private fun handleOpenAction(
    context: Context,
    content: String,
    type: String,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    try {
        when {
            content.startsWith("http://", ignoreCase = true) || content.startsWith("https://", ignoreCase = true) -> {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content)))
            }
            content.startsWith("tel:", ignoreCase = true) -> {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(content)))
            }
            content.startsWith("mailto:", ignoreCase = true) -> {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(content)))
            }
            else -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (content.startsWith("http")) content else "https://$content"))
                context.startActivity(intent)
            }
        }
    } catch (e: Exception) {
        scope.launch {
            snackbarHostState.showSnackbar("Cannot open link directly.")
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onSuccess: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { onSuccess(it) }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
