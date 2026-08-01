package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Transform
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var savedUri by remember { mutableStateOf<Uri?>(null) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Resize", "Crop", "Convert", "Compress", "Rotate")

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
                        withContext(Dispatchers.Main) {
                            originalBitmap = bmp
                            editedBitmap = bmp
                            savedUri = null
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        withContext(Dispatchers.Main) {
                            originalBitmap = bmp
                            editedBitmap = bmp
                            savedUri = null
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Image Tools 5-in-1",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = DarkBackground)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val uri = FileUtil.createTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Take Photo", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                editedBitmap?.let { bmp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Image Preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Text(
                        text = "Dimensions: ${bmp.width} x ${bmp.height} px",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    when (selectedTabIndex) {
                        0 -> ResizeTab(bmp) { newBmp -> editedBitmap = newBmp }
                        1 -> CropTab(bmp) { newBmp -> editedBitmap = newBmp }
                        2 -> ConvertTab(context, bmp, scope, snackbarHostState) { newUri -> savedUri = newUri }
                        3 -> CompressTab(context, bmp, scope, snackbarHostState) { newUri -> savedUri = newUri }
                        4 -> RotateTab(bmp) { newBmp -> editedBitmap = newBmp }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val uri = FileUtil.saveBitmapToGallery(context, bmp, "Image_${System.currentTimeMillis()}")
                                savedUri = uri
                                if (uri != null) {
                                    scope.launch { snackbarHostState.showSnackbar("Saved image to Pictures/MuftTools!") }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = DarkBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Image", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val uri = savedUri ?: FileUtil.saveBitmapToGallery(context, bmp, "Image_Share")
                                uri?.let { FileUtil.shareFile(context, it, "image/png") }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Image", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResizeTab(
    currentBitmap: Bitmap,
    onBitmapUpdated: (Bitmap) -> Unit
) {
    var scalePercent by remember { mutableFloatStateOf(0.75f) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Resize Scale: ${(scalePercent * 100).toInt()}%", fontWeight = FontWeight.Bold, color = TextPrimary)
        Slider(
            value = scalePercent,
            onValueChange = { scalePercent = it },
            valueRange = 0.1f..1.5f,
            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
        )

        Button(
            onClick = {
                val newW = (currentBitmap.width * scalePercent).toInt().coerceAtLeast(10)
                val newH = (currentBitmap.height * scalePercent).toInt().coerceAtLeast(10)
                val resized = Bitmap.createScaledBitmap(currentBitmap, newW, newH, true)
                onBitmapUpdated(resized)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(imageVector = Icons.Default.Transform, contentDescription = null, tint = DarkBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply Resize", color = DarkBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CropTab(
    currentBitmap: Bitmap,
    onBitmapUpdated: (Bitmap) -> Unit
) {
    val presets = listOf("Square (1:1)", "LandScape (16:9)", "Portrait (4:3)")
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Crop Aspect Ratio Presets", fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            presets.forEachIndexed { index, label ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedPresetIndex = index },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPresetIndex == index) CyanPrimary else DarkSurfaceVariant
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedPresetIndex == index) DarkBackground else TextPrimary
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val w = currentBitmap.width
                val h = currentBitmap.height
                val cropped = when (selectedPresetIndex) {
                    0 -> { // 1:1
                        val size = Math.min(w, h)
                        Bitmap.createBitmap(currentBitmap, (w - size) / 2, (h - size) / 2, size, size)
                    }
                    1 -> { // 16:9
                        val targetH = (w * 9 / 16).coerceAtMost(h)
                        Bitmap.createBitmap(currentBitmap, 0, (h - targetH) / 2, w, targetH)
                    }
                    else -> { // 4:3
                        val targetW = (h * 4 / 3).coerceAtMost(w)
                        Bitmap.createBitmap(currentBitmap, (w - targetW) / 2, 0, targetW, h)
                    }
                }
                onBitmapUpdated(cropped)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(imageVector = Icons.Default.Crop, contentDescription = null, tint = DarkBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crop Image Center", color = DarkBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConvertTab(
    context: Context,
    currentBitmap: Bitmap,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSaved: (Uri?) -> Unit
) {
    val formats = listOf("PNG", "JPEG", "WEBP")
    var selectedFormatIndex by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Target Output Format", fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            formats.forEachIndexed { index, label ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedFormatIndex = index },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedFormatIndex == index) CyanPrimary else DarkSurfaceVariant
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedFormatIndex == index) DarkBackground else TextPrimary
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val format = when (selectedFormatIndex) {
                    1 -> Bitmap.CompressFormat.JPEG
                    2 -> Bitmap.CompressFormat.WEBP
                    else -> Bitmap.CompressFormat.PNG
                }
                val uri = FileUtil.saveBitmapToGallery(context, currentBitmap, "Converted_${System.currentTimeMillis()}", format)
                onSaved(uri)
                scope.launch { snackbarHostState.showSnackbar("Converted & saved as ${formats[selectedFormatIndex]}!") }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Text("Convert & Save", color = DarkBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompressTab(
    context: Context,
    currentBitmap: Bitmap,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onSaved: (Uri?) -> Unit
) {
    var quality by remember { mutableFloatStateOf(70f) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("JPEG Compression Quality: ${quality.toInt()}%", fontWeight = FontWeight.Bold, color = TextPrimary)
        Slider(
            value = quality,
            onValueChange = { quality = it },
            valueRange = 10f..100f,
            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
        )

        Button(
            onClick = {
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality.toInt(), stream)
                val byteArray = stream.toByteArray()
                val compressedBmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                val uri = FileUtil.saveBitmapToGallery(context, compressedBmp, "Compressed_${System.currentTimeMillis()}", Bitmap.CompressFormat.JPEG)
                onSaved(uri)
                val sizeStr = FileUtil.getFileSizeString(byteArray.size.toLong())
                scope.launch { snackbarHostState.showSnackbar("Compressed to $sizeStr & saved!") }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(imageVector = Icons.Default.Compress, contentDescription = null, tint = DarkBackground)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compress & Save Image", color = DarkBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RotateTab(
    currentBitmap: Bitmap,
    onBitmapUpdated: (Bitmap) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                val matrix = Matrix().apply { postRotate(90f) }
                val rotated = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.width, currentBitmap.height, matrix, true)
                onBitmapUpdated(rotated)
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
            Text("Rotate 90°", color = TextPrimary)
        }

        Button(
            onClick = {
                val matrix = Matrix().apply { postScale(-1f, 1f) }
                val flipped = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.width, currentBitmap.height, matrix, true)
                onBitmapUpdated(flipped)
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
            Text("Flip Horiz", color = TextPrimary)
        }

        Button(
            onClick = {
                val matrix = Matrix().apply { postScale(1f, -1f) }
                val flipped = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.width, currentBitmap.height, matrix, true)
                onBitmapUpdated(flipped)
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
        ) {
            Text("Flip Vert", color = TextPrimary)
        }
    }
}
