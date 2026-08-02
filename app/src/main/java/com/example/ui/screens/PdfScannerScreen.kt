package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Compress
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ToolRepository
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
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.multipdf.Splitter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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
    val tool = ToolRepository.defaultTools.first { it.id == "pdf_scanner" }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scan PDF", "Merge", "Split", "Compress")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PDF Scanner & Tools",
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
                                color = if (selectedTabIndex == index) CyanPrimary else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> ScanPdfTab(context, scope, snackbarHostState)
                1 -> MergePdfTab(context, scope, snackbarHostState)
                2 -> SplitPdfTab(context, scope, snackbarHostState)
                3 -> CompressPdfTab(context, scope, snackbarHostState)
            }
        }
    }
}
}

@Composable
private fun ScanPdfTab(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val capturedBitmaps = remember { mutableStateListOf<Bitmap>() }
    var pdfTitle by remember { mutableStateOf("Scanned_Document") }
    var isProcessing by remember { mutableStateOf(false) }
    var showCameraPreview by remember { mutableStateOf(false) }
    var generatedPdfUri by remember { mutableStateOf<Uri?>(null) }

    val imageCapture = remember { ImageCapture.Builder().build() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        scope.launch(Dispatchers.IO) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                capturedBitmaps.add(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showCameraPreview = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to scan pages.")
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
        if (showCameraPreview) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        imageCapture = imageCapture
                    )
                    IconButton(
                        onClick = { showCameraPreview = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(DarkBackground.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Close Camera",
                            tint = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            val tempFile = File.createTempFile("scan_", ".jpg", context.cacheDir)
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                                        if (bitmap != null) {
                                            capturedBitmaps.add(bitmap)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Page captured!")
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Failed to capture: ${exception.message}")
                                        }
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capture Page (${capturedBitmaps.size})", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use Camera", color = DarkBackground, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Gallery", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (capturedBitmaps.isNotEmpty()) {
            Text(
                text = "Scanned Pages (${capturedBitmaps.size})",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(capturedBitmaps) { index, bitmap ->
                    Box(
                        modifier = Modifier
                            .size(120.dp, 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                                .clickable { capturedBitmaps.removeAt(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Page",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                                .background(DarkBackground.copy(alpha = 0.7f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = pdfTitle,
                onValueChange = { pdfTitle = it },
                label = { Text("PDF Document Name") },
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

            Button(
                onClick = {
                    if (capturedBitmaps.isEmpty()) return@Button
                    isProcessing = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            val pdfDocument = PdfDocument()
                            capturedBitmaps.forEachIndexed { index, bitmap ->
                                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                                val page = pdfDocument.startPage(pageInfo)
                                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                                pdfDocument.finishPage(page)
                            }
                            val outputFile = File(context.cacheDir, "$pdfTitle.pdf")
                            FileOutputStream(outputFile).use { out ->
                                pdfDocument.writeTo(out)
                            }
                            pdfDocument.close()

                            val savedUri = FileUtil.savePdfToDownloads(context, outputFile, pdfTitle)
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                generatedPdfUri = savedUri
                                snackbarHostState.showSnackbar("PDF generated and saved to Downloads!")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                snackbarHostState.showSnackbar("Failed to generate PDF: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating PDF...", color = DarkBackground)
                } else {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create PDF Document", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }

            generatedPdfUri?.let { uri ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { FileUtil.shareFile(context, uri, "application/pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = DarkBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Generated PDF", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MergePdfTab(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val selectedPdfUris = remember { mutableStateListOf<Uri>() }
    var mergedFileName by remember { mutableStateOf("Merged_Document") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultUri by remember { mutableStateOf<Uri?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedPdfUris.addAll(uris)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { pdfPickerLauncher.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DarkBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PDF Files to Merge", color = DarkBackground, fontWeight = FontWeight.Bold)
        }

        if (selectedPdfUris.isNotEmpty()) {
            Text("Selected PDFs (${selectedPdfUris.size})", fontWeight = FontWeight.Bold, color = TextPrimary)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedPdfUris.forEachIndexed { index, uri ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = CyanPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uri.lastPathSegment ?: "PDF ${index + 1}",
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                            IconButton(onClick = { selectedPdfUris.removeAt(index) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = mergedFileName,
                onValueChange = { mergedFileName = it },
                label = { Text("Merged Output File Name") },
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

            Button(
                onClick = {
                    if (selectedPdfUris.size < 2) {
                        scope.launch { snackbarHostState.showSnackbar("Please select at least 2 PDF files to merge.") }
                        return@Button
                    }
                    isProcessing = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            val merger = PDFMergerUtility()
                            val tempMergedFile = File(context.cacheDir, "$mergedFileName.pdf")
                            merger.destinationFileName = tempMergedFile.absolutePath

                            val tempFiles = mutableListOf<File>()
                            selectedPdfUris.forEach { uri ->
                                FileUtil.getFileFromUri(context, uri)?.let { file ->
                                    tempFiles.add(file)
                                    merger.addSource(file)
                                }
                            }

                            merger.mergeDocuments(null)
                            val savedUri = FileUtil.savePdfToDownloads(context, tempMergedFile, mergedFileName)

                            tempFiles.forEach { it.delete() }

                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                resultUri = savedUri
                                snackbarHostState.showSnackbar("Merged PDF saved to Downloads!")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                snackbarHostState.showSnackbar("Merge failed: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Merging...", color = DarkBackground)
                } else {
                    Icon(imageVector = Icons.AutoMirrored.Filled.MergeType, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Merge PDFs Now", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }

            resultUri?.let { uri ->
                Button(
                    onClick = { FileUtil.shareFile(context, uri, "application/pdf") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Merged PDF", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SplitPdfTab(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var totalPages by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var splitResultMsg by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPdfUri = uri
            scope.launch(Dispatchers.IO) {
                try {
                    val file = FileUtil.getFileFromUri(context, uri)
                    if (file != null) {
                        val document = PDDocument.load(file)
                        val pages = document.numberOfPages
                        document.close()
                        file.delete()
                        withContext(Dispatchers.Main) {
                            totalPages = pages
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
        Button(
            onClick = { pdfPickerLauncher.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, tint = DarkBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PDF to Split", color = DarkBackground, fontWeight = FontWeight.Bold)
        }

        selectedPdfUri?.let { uri ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PDF Loaded", color = CyanPrimary, fontWeight = FontWeight.Bold)
                    Text("Total Pages: $totalPages", color = TextPrimary)
                }
            }

            Button(
                onClick = {
                    isProcessing = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            val tempFile = FileUtil.getFileFromUri(context, uri)
                            if (tempFile != null) {
                                val document = PDDocument.load(tempFile)
                                val splitter = Splitter()
                                val pages = splitter.split(document)

                                var savedCount = 0
                                pages.forEachIndexed { i, pageDoc ->
                                    val pageFile = File(context.cacheDir, "split_page_${i + 1}.pdf")
                                    pageDoc.save(pageFile)
                                    pageDoc.close()
                                    FileUtil.savePdfToDownloads(context, pageFile, "Split_Page_${i + 1}")
                                    pageFile.delete()
                                    savedCount++
                                }
                                document.close()
                                tempFile.delete()

                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    splitResultMsg = "Successfully split into $savedCount pages! Check Downloads folder."
                                    snackbarHostState.showSnackbar(splitResultMsg)
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                snackbarHostState.showSnackbar("Split failed: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing && totalPages > 0,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Splitting Pages...", color = DarkBackground)
                } else {
                    Icon(imageVector = Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Split Into Single Pages", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }

            if (splitResultMsg.isNotEmpty()) {
                Text(splitResultMsg, color = EmeraldTertiary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CompressPdfTab(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var originalSizeStr by remember { mutableStateOf("") }
    var compressedSizeStr by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var compressedUri by remember { mutableStateOf<Uri?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPdfUri = uri
            FileUtil.getFileFromUri(context, uri)?.let { file ->
                originalSizeStr = FileUtil.getFileSizeString(file.length())
                file.delete()
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
        Button(
            onClick = { pdfPickerLauncher.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(imageVector = Icons.Default.Compress, contentDescription = null, tint = DarkBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PDF to Compress", color = DarkBackground, fontWeight = FontWeight.Bold)
        }

        selectedPdfUri?.let { uri ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Original Size: $originalSizeStr", color = TextPrimary, fontWeight = FontWeight.Bold)
                    if (compressedSizeStr.isNotEmpty()) {
                        Text("Compressed Size: $compressedSizeStr", color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    isProcessing = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            val tempFile = FileUtil.getFileFromUri(context, uri)
                            if (tempFile != null) {
                                val document = PDDocument.load(tempFile)
                                val compressedFile = File(context.cacheDir, "Compressed_Document.pdf")
                                document.save(compressedFile)
                                document.close()
                                tempFile.delete()

                                val savedUri = FileUtil.savePdfToDownloads(context, compressedFile, "Compressed_Document")
                                val compSize = FileUtil.getFileSizeString(compressedFile.length())
                                compressedFile.delete()

                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    compressedSizeStr = compSize
                                    compressedUri = savedUri
                                    snackbarHostState.showSnackbar("PDF Compressed and saved to Downloads!")
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                snackbarHostState.showSnackbar("Compression failed: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compressing PDF...", color = DarkBackground)
                } else {
                    Icon(imageVector = Icons.Default.Compress, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compress PDF Now", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }

            compressedUri?.let { resUri ->
                Button(
                    onClick = { FileUtil.shareFile(context, resUri, "application/pdf") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Compressed PDF", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
