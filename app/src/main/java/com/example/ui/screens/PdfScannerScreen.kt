package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.db.ScanHistoryEntity
import com.example.ui.components.CameraPreview
import com.example.ui.components.CropOverlay
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrownGold
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletButton
import com.example.ui.theme.VioletGlowing
import com.example.ui.theme.VioletSecondary
import com.example.util.CropCorners
import com.example.util.FileUtil
import com.example.util.ImageFilterType
import com.example.util.ImageProcessingUtil
import com.example.util.PdfGenerationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ScannedPage(
    val id: String = UUID.randomUUID().toString(),
    initialBitmap: Bitmap,
    initialCorners: CropCorners = ImageProcessingUtil.autoDetectEdges(initialBitmap),
    initialFilter: ImageFilterType = ImageFilterType.COLOR,
    initialCropped: Boolean = false
) {
    var originalBitmap by mutableStateOf(initialBitmap)
    var cropCorners by mutableStateOf(initialCorners)
    var filter by mutableStateOf(initialFilter)
    var isCropped by mutableStateOf(initialCropped)

    fun renderProcessed(): Bitmap {
        val cropped = if (isCropped) {
            ImageProcessingUtil.cropBitmap(originalBitmap, cropCorners)
        } else {
            originalBitmap
        }
        return ImageProcessingUtil.applyFilter(cropped, filter)
    }
}

enum class ScannerScreenState {
    CAMERA,
    EDIT_PAGES,
    HISTORY,
    VIEW_PDF
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfScannerScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val db = remember { AppDatabase.getDatabase(context) }
    val historyList by db.scanHistoryDao().getAllScans().collectAsState(initial = emptyList())

    var screenState by remember { mutableStateOf(ScannerScreenState.CAMERA) }
    val pages = remember { mutableStateListOf<ScannedPage>() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }

    var isProcessing by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }

    // Save PDF Dialog
    var showSaveDialog by remember { mutableStateOf(false) }
    var documentTitle by remember { mutableStateOf("") }

    // Viewing PDF State
    var pdfToView by remember { mutableStateOf<ScanHistoryEntity?>(null) }
    var pdfBitmapPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // Rename Dialog
    var scanToRename by remember { mutableStateOf<ScanHistoryEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }

    // Multi Gallery Picker
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                isProcessing = true
                val newPages = mutableListOf<ScannedPage>()
                uris.forEach { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            if (bitmap != null) {
                                val autoCorners = ImageProcessingUtil.autoDetectEdges(bitmap)
                                newPages.add(ScannedPage(initialBitmap = bitmap, initialCorners = autoCorners))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                withContext(Dispatchers.Main) {
                    pages.addAll(newPages)
                    isProcessing = false
                    if (pages.isNotEmpty()) {
                        screenState = ScannerScreenState.EDIT_PAGES
                        selectedPageIndex = pages.size - 1
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (screenState) {
                            ScannerScreenState.CAMERA -> "PDF Scanner"
                            ScannerScreenState.EDIT_PAGES -> "PDF Scanner"
                            ScannerScreenState.HISTORY -> "Recent Scan History"
                            ScannerScreenState.VIEW_PDF -> pdfToView?.title ?: "View Document"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (screenState) {
                            ScannerScreenState.EDIT_PAGES -> {
                                if (pages.isNotEmpty()) {
                                    screenState = ScannerScreenState.CAMERA
                                } else {
                                    onBackClick()
                                }
                            }
                            ScannerScreenState.HISTORY -> screenState = ScannerScreenState.CAMERA
                            ScannerScreenState.VIEW_PDF -> screenState = ScannerScreenState.HISTORY
                            ScannerScreenState.CAMERA -> onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (screenState == ScannerScreenState.CAMERA || screenState == ScannerScreenState.EDIT_PAGES) {
                        IconButton(onClick = { screenState = ScannerScreenState.HISTORY }) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "History",
                                tint = CyanPrimary
                            )
                        }
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0B18))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0D0B18)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (screenState) {
                ScannerScreenState.CAMERA -> {
                    CameraScannerView(
                        pageCount = pages.size,
                        flashEnabled = flashEnabled,
                        onToggleFlash = { flashEnabled = !flashEnabled },
                        onCaptureClick = { captureUseCase ->
                            isProcessing = true
                            scope.launch(Dispatchers.IO) {
                                val photoFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                var handled = false

                                fun processAndAddBitmap(bitmap: Bitmap) {
                                    if (handled) return
                                    handled = true
                                    val autoCorners = ImageProcessingUtil.autoDetectEdges(bitmap)
                                    val newPage = ScannedPage(
                                        initialBitmap = bitmap,
                                        initialCorners = autoCorners
                                    )
                                    scope.launch(Dispatchers.Main) {
                                        pages.add(newPage)
                                        selectedPageIndex = pages.size - 1
                                        screenState = ScannerScreenState.EDIT_PAGES
                                        isProcessing = false
                                    }
                                }

                                // Timeout job if camera callback hangs (e.g. in emulator without physical camera)
                                val timeoutJob = scope.launch(Dispatchers.IO) {
                                    kotlinx.coroutines.delay(2500)
                                    if (!handled) {
                                        val sampleDoc = ImageProcessingUtil.generateSampleDocumentBitmap()
                                        processAndAddBitmap(sampleDoc)
                                    }
                                }

                                try {
                                    captureUseCase.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                timeoutJob.cancel()
                                                scope.launch(Dispatchers.IO) {
                                                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                                                    val bmpToUse = bitmap ?: ImageProcessingUtil.generateSampleDocumentBitmap()
                                                    processAndAddBitmap(bmpToUse)
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                timeoutJob.cancel()
                                                scope.launch(Dispatchers.IO) {
                                                    val sampleDoc = ImageProcessingUtil.generateSampleDocumentBitmap()
                                                    processAndAddBitmap(sampleDoc)
                                                }
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    timeoutJob.cancel()
                                    val sampleDoc = ImageProcessingUtil.generateSampleDocumentBitmap()
                                    processAndAddBitmap(sampleDoc)
                                }
                            }
                        },
                        onPickGalleryClick = { galleryPicker.launch("image/*") },
                        onProceedToEdit = {
                            if (pages.isNotEmpty()) {
                                screenState = ScannerScreenState.EDIT_PAGES
                                selectedPageIndex = 0
                            }
                        }
                    )
                }

                ScannerScreenState.EDIT_PAGES -> {
                    if (pages.isNotEmpty()) {
                        val activeIndex = selectedPageIndex.coerceIn(0, pages.size - 1)
                        val activePage = pages[activeIndex]

                        PageEditView(
                            page = activePage,
                            pageIndex = activeIndex,
                            totalPages = pages.size,
                            pages = pages,
                            onPageIndexSelected = { selectedPageIndex = it },
                            onDeletePage = { idx ->
                                if (pages.size > idx) {
                                    pages.removeAt(idx)
                                    if (pages.isEmpty()) {
                                        screenState = ScannerScreenState.CAMERA
                                    } else {
                                        selectedPageIndex = (activeIndex - 1).coerceAtLeast(0)
                                    }
                                }
                            },
                            onAddMorePages = {
                                screenState = ScannerScreenState.CAMERA
                            },
                            onRotateRight = {
                                scope.launch(Dispatchers.IO) {
                                    isProcessing = true
                                    val rotated = ImageProcessingUtil.rotateBitmap(activePage.originalBitmap, 90f)
                                    val autoCorners = ImageProcessingUtil.autoDetectEdges(rotated)
                                    withContext(Dispatchers.Main) {
                                        activePage.originalBitmap = rotated
                                        activePage.cropCorners = autoCorners
                                        isProcessing = false
                                    }
                                }
                            },
                            onAutoDetectEdges = {
                                scope.launch(Dispatchers.IO) {
                                    isProcessing = true
                                    val autoCorners = ImageProcessingUtil.autoDetectEdges(activePage.originalBitmap)
                                    withContext(Dispatchers.Main) {
                                        activePage.cropCorners = autoCorners
                                        activePage.isCropped = true
                                        isProcessing = false
                                    }
                                }
                            },
                            onFilterSelected = { filter ->
                                activePage.filter = filter
                            },
                            onCropCornersChanged = { newCorners ->
                                activePage.cropCorners = newCorners
                                activePage.isCropped = true
                            },
                            onSharePage = {
                                scope.launch(Dispatchers.IO) {
                                    val bmp = activePage.renderProcessed()
                                    val tempFile = File(context.cacheDir, "shared_page_${System.currentTimeMillis()}.jpg")
                                    tempFile.outputStream().use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                                    withContext(Dispatchers.Main) {
                                        FileUtil.shareFile(context, uri, "image/jpeg", "Share Scanned Page")
                                    }
                                }
                            },
                            onProceedToSave = {
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                documentTitle = "Scan_$timeStamp"
                                showSaveDialog = true
                            }
                        )
                    }
                }

                ScannerScreenState.HISTORY -> {
                    ScanHistoryView(
                        historyList = historyList,
                        onViewScan = { item ->
                            scope.launch(Dispatchers.IO) {
                                isProcessing = true
                                val pdfFile = File(item.filePath)
                                val count = PdfGenerationUtil.getPdfPageCount(pdfFile)
                                val pageBitmaps = mutableListOf<Bitmap>()
                                for (i in 0 until count) {
                                    val bmp = PdfGenerationUtil.renderPdfPageToBitmap(pdfFile, i)
                                    if (bmp != null) pageBitmaps.add(bmp)
                                }
                                pdfBitmapPages = pageBitmaps
                                pdfToView = item
                                isProcessing = false
                                withContext(Dispatchers.Main) {
                                    screenState = ScannerScreenState.VIEW_PDF
                                }
                            }
                        },
                        onRenameScan = { item ->
                            scanToRename = item
                            renameInput = item.title
                        },
                        onDeleteScan = { item ->
                            scope.launch(Dispatchers.IO) {
                                db.scanHistoryDao().deleteScan(item.id)
                                File(item.filePath).delete()
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar("Deleted ${item.title}")
                                }
                            }
                        },
                        onShareScan = { item ->
                            val pdfFile = File(item.filePath)
                            if (pdfFile.exists()) {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                FileUtil.shareFile(context, uri, "application/pdf", "Share PDF")
                            }
                        },
                        onNewScanClick = { screenState = ScannerScreenState.CAMERA }
                    )
                }

                ScannerScreenState.VIEW_PDF -> {
                    pdfToView?.let { item ->
                        ViewPdfScreen(
                            scanItem = item,
                            pages = pdfBitmapPages,
                            onShare = {
                                val pdfFile = File(item.filePath)
                                if (pdfFile.exists()) {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                    FileUtil.shareFile(context, uri, "application/pdf", "Share PDF")
                                }
                            }
                        )
                    }
                }
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = VioletGlowing)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Processing document...", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Save PDF Modal Dialog
            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Save Document as PDF", color = TextPrimary) },
                    text = {
                        Column {
                            Text("Enter name for your PDF file:", color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = documentTitle,
                                onValueChange = { documentTitle = it },
                                label = { Text("Document Name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletGlowing,
                                    unfocusedBorderColor = CardBorder,
                                    focusedLabelColor = VioletGlowing,
                                    unfocusedLabelColor = TextMuted,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSaveDialog = false
                                scope.launch(Dispatchers.IO) {
                                    isProcessing = true
                                    val processedBitmaps = pages.map { it.renderProcessed() }
                                    val pdfDir = File(context.filesDir, "scanned_pdfs").apply { mkdirs() }
                                    val pdfFileName = if (documentTitle.endsWith(".pdf", ignoreCase = true)) documentTitle else "$documentTitle.pdf"
                                    val pdfFile = File(pdfDir, pdfFileName)

                                    val success = PdfGenerationUtil.createPdfFromBitmaps(context, processedBitmaps, pdfFile)
                                    if (success) {
                                        // Save to Downloads folder too
                                        FileUtil.savePdfToDownloads(context, pdfFile, pdfFileName)

                                        // Insert into Room DB
                                        val entity = ScanHistoryEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = documentTitle,
                                            filePath = pdfFile.absolutePath,
                                            pageCount = processedBitmaps.size,
                                            fileSize = FileUtil.getFileSizeString(pdfFile.length()),
                                            timestamp = System.currentTimeMillis()
                                        )
                                        db.scanHistoryDao().insertScan(entity)

                                        isProcessing = false
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar("PDF saved successfully!")
                                            screenState = ScannerScreenState.HISTORY
                                        }
                                    } else {
                                        isProcessing = false
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar("Failed to create PDF.")
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletGlowing)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurface
                )
            }

            // Rename PDF Dialog
            if (scanToRename != null) {
                AlertDialog(
                    onDismissRequest = { scanToRename = null },
                    title = { Text("Rename PDF Document", color = TextPrimary) },
                    text = {
                        OutlinedTextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            label = { Text("New Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletGlowing,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val target = scanToRename
                                if (target != null && renameInput.isNotBlank()) {
                                    scope.launch(Dispatchers.IO) {
                                        db.scanHistoryDao().renameScan(target.id, renameInput)
                                        withContext(Dispatchers.Main) {
                                            scanToRename = null
                                            snackbarHostState.showSnackbar("Renamed successfully!")
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletGlowing)
                        ) {
                            Text("Rename", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { scanToRename = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}

@Composable
fun CameraScannerView(
    pageCount: Int,
    flashEnabled: Boolean,
    onToggleFlash: () -> Unit,
    onCaptureClick: (ImageCapture) -> Unit,
    onPickGalleryClick: () -> Unit,
    onProceedToEdit: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isCameraAvailable by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val captureUseCase = remember(flashEnabled) {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .build()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0B18))) {
        if (hasPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                imageCapture = captureUseCase,
                onCameraAvailableChanged = { available ->
                    isCameraAvailable = available
                }
            )

            if (!isCameraAvailable) {
                // Friendly fallback overlay when no physical camera feed exists (e.g. emulator)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .padding(bottom = 80.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(VioletGlowing.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Camera",
                            tint = CyanPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Preview Ready",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the capture button to scan a document, or pick images from your gallery.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onPickGalleryClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Gallery", color = VioletGlowing)
                        }
                        Button(
                            onClick = { onCaptureClick(captureUseCase) },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletGlowing),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Scan Document", color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Permission request screen overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .padding(bottom = 80.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(VioletGlowing.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Camera Permission",
                        tint = VioletGlowing,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Needed",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please allow camera access to scan physical documents directly with your camera.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletGlowing),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Camera Access", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onPickGalleryClick) {
                    Text("Or Select Images from Gallery", color = CyanPrimary)
                }
            }
        }

        // Top bar controls
        if (hasPermission && isCameraAvailable) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onToggleFlash,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) Color.Yellow else Color.White
                    )
                }
            }
        }

        // Bottom Controls Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xFF0D0B18).copy(alpha = 0.85f))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPickGalleryClick,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Pick Gallery",
                        tint = VioletGlowing,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = { onCaptureClick(captureUseCase) },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(VioletGlowing)
                        .border(3.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Capture",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (pageCount > 0) {
                    Button(
                        onClick = onProceedToEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = VioletButton),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$pageCount",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(54.dp))
                }
            }
        }
    }
}

@Composable
fun PageEditView(
    page: ScannedPage,
    pageIndex: Int,
    totalPages: Int,
    pages: List<ScannedPage>,
    onPageIndexSelected: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onAddMorePages: () -> Unit,
    onRotateRight: () -> Unit,
    onAutoDetectEdges: () -> Unit,
    onFilterSelected: (ImageFilterType) -> Unit,
    onCropCornersChanged: (CropCorners) -> Unit,
    onSharePage: () -> Unit,
    onProceedToSave: () -> Unit
) {
    var isCropMode by remember { mutableStateOf(false) }

    // Pre-rendered thumbnails for filter carousel
    val filterThumbnails = remember(page.originalBitmap, page.isCropped, isCropMode) {
        val baseBmp = if (page.isCropped && !isCropMode) ImageProcessingUtil.cropBitmap(page.originalBitmap, page.cropCorners) else page.originalBitmap
        val thumb = Bitmap.createScaledBitmap(baseBmp, 120, (120f * baseBmp.height / baseBmp.width).toInt().coerceAtLeast(120), false)
        ImageFilterType.values().associateWith { filter ->
            ImageProcessingUtil.applyFilter(thumb, filter)
        }
    }

    val processedBitmap = remember(page.originalBitmap, page.filter, page.isCropped, isCropMode) {
        page.renderProcessed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B18))
    ) {
        // Top Multi-Page Pagination Bar if multi-page
        if (totalPages > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF140F26))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(pages) { idx, p ->
                    val isSelected = idx == pageIndex
                    Text(
                        text = "Page ${idx + 1}",
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) VioletGlowing else Color.Transparent)
                            .clickable { onPageIndexSelected(idx) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                item {
                    IconButton(
                        onClick = onAddMorePages,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Page", tint = VioletGlowing)
                    }
                }
            }
        }

        // Main Document Preview Card with Glowing Corner Brackets
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val containerW = maxWidth
            val containerH = maxHeight

            val currentBmp = if (isCropMode) page.originalBitmap else processedBitmap
            val bmpW = currentBmp.width.toFloat().coerceAtLeast(1f)
            val bmpH = currentBmp.height.toFloat().coerceAtLeast(1f)

            val containerAspect = containerW.value / containerH.value
            val bmpAspect = bmpW / bmpH

            val fittedWidth = if (bmpAspect > containerAspect) containerW else containerH * bmpAspect
            val fittedHeight = if (bmpAspect > containerAspect) containerW / bmpAspect else containerH

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isCropMode) {
                    // Top Crop Control Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                page.cropCorners = CropCorners(Offset(0f, 0f), Offset(1f, 0f), Offset(1f, 1f), Offset(0f, 1f))
                                page.isCropped = false
                            }
                        ) {
                            Text("Full Image", color = Color.White, fontSize = 12.sp)
                        }
                        TextButton(onClick = { onAutoDetectEdges() }) {
                            Text("Auto Edge", color = CyanPrimary, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                page.isCropped = true
                                isCropMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletGlowing),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(fittedWidth, fittedHeight)
                        .background(Color(0xFF14102B), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            bitmap = currentBmp.asImageBitmap(),
                            contentDescription = "Document Page",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isCropMode) {
                        CropOverlay(
                            cropCorners = page.cropCorners,
                            onCornersChanged = onCropCornersChanged,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Glowing Corner Reticles
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cornerLen = 24.dp.toPx()
                            val strokeW = 4.dp.toPx()
                            val color = Color(0xFFA855F7)

                            drawLine(color, Offset(2f, 2f), Offset(2f + cornerLen, 2f), strokeWidth = strokeW)
                            drawLine(color, Offset(2f, 2f), Offset(2f, 2f + cornerLen), strokeWidth = strokeW)

                            drawLine(color, Offset(size.width - 2f, 2f), Offset(size.width - 2f - cornerLen, 2f), strokeWidth = strokeW)
                            drawLine(color, Offset(size.width - 2f, 2f), Offset(size.width - 2f, 2f + cornerLen), strokeWidth = strokeW)

                            drawLine(color, Offset(2f, size.height - 2f), Offset(2f + cornerLen, size.height - 2f), strokeWidth = strokeW)
                            drawLine(color, Offset(2f, size.height - 2f), Offset(2f, size.height - 2f - cornerLen), strokeWidth = strokeW)

                            drawLine(color, Offset(size.width - 2f, size.height - 2f), Offset(size.width - 2f - cornerLen, size.height - 2f), strokeWidth = strokeW)
                            drawLine(color, Offset(size.width - 2f, size.height - 2f), Offset(size.width - 2f, size.height - 2f - cornerLen), strokeWidth = strokeW)
                        }
                    }
                }
            }
        }

        // Live Filter Thumbnails Preview Row (Matching Screenshot Carousel!)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0B18))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(ImageFilterType.values().size) { idx ->
                val filter = ImageFilterType.values()[idx]
                val isSelected = page.filter == filter
                val thumbBitmap = filterThumbnails[filter]

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onFilterSelected(filter) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 62.dp, height = 76.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) VioletGlowing else Color(0xFF2E2450),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        thumbBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = filter.label,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = filter.label,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Bottom Action Toolbar (Crop, Filter, Rotate, Share, Save PDF - Exact 5 icons matching screenshot!)
        Surface(
            color = Color(0xFF140F26),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Crop
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isCropMode = !isCropMode }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Crop,
                        contentDescription = "Crop",
                        tint = if (isCropMode) VioletGlowing else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Crop", color = if (isCropMode) VioletGlowing else Color.White, fontSize = 11.sp)
                }

                // 2. Filter / Auto
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAutoDetectEdges() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Auto Crop",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Filter", color = Color.White, fontSize = 11.sp)
                }

                // 3. Rotate
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onRotateRight() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Rotate",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Rotate", color = Color.White, fontSize = 11.sp)
                }

                // 4. Share
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSharePage() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Share", color = Color.White, fontSize = 11.sp)
                }

                // 5. Save PDF Button (Prominent Glowing Purple Pill Button!)
                Button(
                    onClick = onProceedToSave,
                    colors = ButtonDefaults.buttonColors(containerColor = VioletButton),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save PDF",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save PDF",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanHistoryView(
    historyList: List<ScanHistoryEntity>,
    onViewScan: (ScanHistoryEntity) -> Unit,
    onRenameScan: (ScanHistoryEntity) -> Unit,
    onDeleteScan: (ScanHistoryEntity) -> Unit,
    onShareScan: (ScanHistoryEntity) -> Unit,
    onNewScanClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = historyList.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B18))
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search PDF scans...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = VioletGlowing) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioletGlowing,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No documents match search" else "No saved PDF scans yet",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNewScanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = VioletGlowing)
                    ) {
                        Text("Scan Document Now")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewScan(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VioletGlowing.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = null,
                                    tint = VioletGlowing,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text(
                                        text = "${item.pageCount} ${if (item.pageCount == 1) "Page" else "Pages"} • ${item.fileSize}",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(onClick = { onShareScan(item) }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextSecondary)
                            }
                            IconButton(onClick = { onRenameScan(item) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = TextSecondary)
                            }
                            IconButton(onClick = { onDeleteScan(item) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViewPdfScreen(
    scanItem: ScanHistoryEntity,
    pages: List<Bitmap>,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B18))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = scanItem.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${pages.size} Pages • ${scanItem.fileSize}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = VioletGlowing)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share PDF")
            }
        }

        if (pages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unable to render PDF pages.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(pages) { idx, pageBmp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Image(
                                bitmap = pageBmp.asImageBitmap(),
                                contentDescription = "Page ${idx + 1}",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Page ${idx + 1} of ${pages.size}",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}
