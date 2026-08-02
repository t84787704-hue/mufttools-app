package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.concurrent.Executors

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

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scan QR Code", "Generate QR Code")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QR Scanner & Generator",
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
            ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = CyanPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CyanPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) CyanPrimary else TextSecondary
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> ScanQrTab(context, scope, snackbarHostState)
                1 -> GenerateQrTab(context, scope, snackbarHostState)
            }
        }
    }
}
}

@Composable
private fun ScanQrTab(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to scan QR codes.")
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
                            scannedResult = barcodes.first().rawValue
                            scope.launch { snackbarHostState.showSnackbar("QR Code detected!") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("No QR code found in image.") }
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
                        if (scannedResult == null || scannedResult != result) {
                            scannedResult = result
                            scope.launch { snackbarHostState.showSnackbar("QR Code scanned!") }
                        }
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasCameraPermission) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(48.dp), tint = CyanPrimary)
                    Text("Live Camera Scanner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Grant camera permission to scan QR & Barcodes live.", color = TextSecondary, fontSize = 13.sp)
                    Button(
                        onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Enable Camera Scanner", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        imageAnalysis = imageAnalysis
                    )
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.Center)
                            .border(2.dp, CyanPrimary, RoundedCornerShape(16.dp))
                    )
                }
            }
        }

        Button(
            onClick = { galleryPickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = TextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Image from Gallery", color = TextPrimary, fontWeight = FontWeight.Bold)
        }

        scannedResult?.let { text ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Scanned Content:", style = MaterialTheme.typography.titleSmall, color = CyanPrimary, fontWeight = FontWeight.Bold)
                    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("QR Code", text)
                                clipboard.setPrimaryClip(clip)
                                scope.launch { snackbarHostState.showSnackbar("Copied to clipboard!") }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = DarkBackground)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (text.startsWith("http://") || text.startsWith("https://")) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = DarkBackground)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Link", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share QR Text"))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
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

@Composable
private fun GenerateQrTab(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    var qrInputText by remember { mutableStateOf("https://mufttools.app") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun generateQrCode(text: String) {
        if (text.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                    }
                }
                withContext(Dispatchers.Main) {
                    qrBitmap = bmp
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Initial QR code generation
    remember(qrInputText) {
        generateQrCode(qrInputText)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = qrInputText,
            onValueChange = {
                qrInputText = it
                generateQrCode(it)
            },
            label = { Text("Enter Text or URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = DarkSurfaceVariant,
                focusedLabelColor = CyanPrimary,
                unfocusedLabelColor = TextMuted,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        qrBitmap?.let { bmp ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val uri = FileUtil.saveBitmapToGallery(context, bmp, "QR_${System.currentTimeMillis()}")
                        if (uri != null) {
                            scope.launch { snackbarHostState.showSnackbar("QR Code saved to Pictures/MuftTools!") }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save QR", color = DarkBackground, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val uri = FileUtil.saveBitmapToGallery(context, bmp, "QR_Share")
                        uri?.let { FileUtil.shareFile(context, it, "image/png") }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share QR", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
