package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.CameraPreview
import com.example.ui.components.CropOverlay
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CropCorners
import com.example.util.ImageFilterType
import com.example.util.ImageProcessingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class ScannedPage(
    val id: String = UUID.randomUUID().toString(),
    var originalBitmap: Bitmap,
    var cropCorners: CropCorners = ImageProcessingUtil.autoDetectEdges(originalBitmap),
    var filter: ImageFilterType = ImageFilterType.MAGIC,
    var isCropped: Boolean = false
) {
    fun renderProcessed(): Bitmap {
        val cropped = if (isCropped) {
            ImageProcessingUtil.cropBitmap(originalBitmap, cropCorners)
        } else {
            originalBitmap
        }
        return ImageProcessingUtil.applyFilter(cropped, filter)
    }
}

enum class ScannerStep {
    CAMERA,
    EDIT_PAGES
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

    var currentStep by remember { mutableStateOf(ScannerStep.CAMERA) }
    val pages = remember { mutableStateListOf<ScannedPage>() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }

    var isProcessing by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Multi Gallery Picker
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                isProcessing = true
                uris.forEach { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream)
                            if (bitmap != null) {
                                val newPage = ScannedPage(originalBitmap = bitmap)
                                pages.add(newPage)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isProcessing = false
                if (pages.isNotEmpty()) {
                    currentStep = ScannerStep.EDIT_PAGES
                    selectedPageIndex = pages.size - 1
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PDF Document Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (currentStep == ScannerStep.CAMERA) "Scan pages using camera or gallery" else "Edit Page ${selectedPageIndex + 1} of ${pages.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == ScannerStep.EDIT_PAGES && pages.isNotEmpty()) {
                            currentStep = ScannerStep.CAMERA
                        } else {
                            onBackClick()
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
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentStep) {
                ScannerStep.CAMERA -> {
                    CameraScannerView(
                        pageCount = pages.size,
                        flashEnabled = flashEnabled,
                        onToggleFlash = { flashEnabled = !flashEnabled },
                        onCaptureClick = { cap ->
                            scope.launch(Dispatchers.IO) {
                                isProcessing = true
                                val photoFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                cap.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            scope.launch(Dispatchers.IO) {
                                                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                                                if (bitmap != null) {
                                                    val newPage = ScannedPage(originalBitmap = bitmap)
                                                    pages.add(newPage)
                                                }
                                                isProcessing = false
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isProcessing = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Capture failed: ${exception.message}")
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        onPickGalleryClick = { galleryPicker.launch("image/*") },
                        onProceedToEdit = {
                            if (pages.isNotEmpty()) {
                                currentStep = ScannerStep.EDIT_PAGES
                                selectedPageIndex = 0
                            }
                        }
                    )
                }

                ScannerStep.EDIT_PAGES -> {
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
                                        currentStep = ScannerStep.CAMERA
                                    } else {
                                        selectedPageIndex = (activeIndex - 1).coerceAtLeast(0)
                                    }
                                }
                            },
                            onAddMorePages = {
                                currentStep = ScannerStep.CAMERA
                            },
                            onRotateLeft = {
                                scope.launch(Dispatchers.IO) {
                                    isProcessing = true
                                    val rotated = ImageProcessingUtil.rotateBitmap(activePage.originalBitmap, -90f)
                                    activePage.originalBitmap = rotated
                                    activePage.cropCorners = ImageProcessingUtil.autoDetectEdges(rotated)
                                    isProcessing = false
                                }
                            },
                            onRotateRight = {
                                scope.launch(Dispatchers.IO) {
                                    isProcessing = true
                                    val rotated = ImageProcessingUtil.rotateBitmap(activePage.originalBitmap, 90f)
                                    activePage.originalBitmap = rotated
                                    activePage.cropCorners = ImageProcessingUtil.autoDetectEdges(rotated)
                                    isProcessing = false
                                }
                            },
                            onAutoDetectEdges = {
                                scope.launch(Dispatchers.IO) {
                                    isProcessing = true
                                    activePage.cropCorners = ImageProcessingUtil.autoDetectEdges(activePage.originalBitmap)
                                    activePage.isCropped = true
                                    isProcessing = false
                                }
                            },
                            onFilterSelected = { filter ->
                                activePage.filter = filter
                            },
                            onCropCornersChanged = { newCorners ->
                                activePage.cropCorners = newCorners
                                activePage.isCropped = true
                            },
                            onProceedToSave = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Phase 1 completed! All ${pages.size} pages processed.")
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
                        CircularProgressIndicator(color = CyanPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Processing document page...", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
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
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Viewfinder
        val captureUseCase = remember {
            ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                .build()
        }

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            imageCapture = captureUseCase
        )

        // Flash Toggle Top
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

        // Camera Action Controls Bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Import from gallery
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
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Shutter Capture Button
                IconButton(
                    onClick = { onCaptureClick(captureUseCase) },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CyanPrimary)
                        .border(3.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Capture",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Proceed Button with Page Badge
                if (pageCount > 0) {
                    Button(
                        onClick = onProceedToEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
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
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onAutoDetectEdges: () -> Unit,
    onFilterSelected: (ImageFilterType) -> Unit,
    onCropCornersChanged: (CropCorners) -> Unit,
    onProceedToSave: () -> Unit
) {
    var editMode by remember { mutableStateOf("CROP") } // CROP, FILTER
    val processedBitmap = remember(page.originalBitmap, page.filter, page.isCropped, page.cropCorners) {
        page.renderProcessed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Toolbar Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRotateLeft) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                        contentDescription = "Rotate Left",
                        tint = TextPrimary
                    )
                }
                IconButton(onClick = onRotateRight) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Rotate Right",
                        tint = TextPrimary
                    )
                }
                IconButton(onClick = onAutoDetectEdges) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Auto Crop",
                        tint = CyanPrimary
                    )
                }
            }

            Row {
                IconButton(onClick = { onDeletePage(pageIndex) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Page",
                        tint = Color.Red
                    )
                }
                Button(
                    onClick = onProceedToSave,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Center Document Display with Interactive Crop Handles or Rendered Filter Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (editMode == "CROP") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        bitmap = page.originalBitmap.asImageBitmap(),
                        contentDescription = "Original Page",
                        modifier = Modifier.fillMaxSize()
                    )
                    CropOverlay(
                        cropCorners = page.cropCorners,
                        onCornersChanged = onCropCornersChanged,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Image(
                    bitmap = processedBitmap.asImageBitmap(),
                    contentDescription = "Processed Page",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        // Edit Mode Tabs (Crop vs Filter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = editMode == "CROP",
                onClick = { editMode = "CROP" },
                label = { Text("Adjust Crop") },
                leadingIcon = { Icon(Icons.Filled.Crop, contentDescription = null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary,
                    selectedLabelColor = Color.Black
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            FilterChip(
                selected = editMode == "FILTER",
                onClick = { editMode = "FILTER" },
                label = { Text("Filters") },
                leadingIcon = { Icon(Icons.Filled.Filter, contentDescription = null) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanPrimary,
                    selectedLabelColor = Color.Black
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Filter Options List (When Filter tab selected)
        if (editMode == "FILTER") {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ImageFilterType.values().size) { idx ->
                    val filter = ImageFilterType.values()[idx]
                    val isSelected = page.filter == filter
                    Card(
                        onClick = { onFilterSelected(filter) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyanPrimary else DarkSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = filter.label,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Bottom Thumbnail Strip for Multi-Page Preview & Navigation
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(pages) { index, item ->
                val isSelected = index == pageIndex
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) CyanPrimary else Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onPageIndexSelected(index) }
                ) {
                    Image(
                        bitmap = item.renderProcessed().asImageBitmap(),
                        contentDescription = "Page ${index + 1}",
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(topStart = 4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Add More Page Button
            item {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, CyanPrimary, RoundedCornerShape(8.dp))
                        .clickable { onAddMorePages() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Page",
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Add", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
