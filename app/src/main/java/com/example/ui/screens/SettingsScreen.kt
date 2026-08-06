package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.VioletSecondary
import com.example.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.theme.CardBorder
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onNavigatePrivacy: () -> Unit,
    onNavigateTerms: () -> Unit,
    onNavigateAbout: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentThemeMode by viewModel.themeMode.collectAsState()
    var cacheSizeStr by remember { mutableStateOf(getCacheSize(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
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
            // App Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.5f), VioletSecondary.copy(alpha = 0.5f))),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(CyanPrimary, VioletSecondary))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Free Tools",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Free Tools",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = EmeraldTertiary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "v1.0.0",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "100% Offline • 100% Ad-Free • Private Processing",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Appearance & Theme Section
            SectionTitle("Appearance & Theme")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme",
                                modifier = Modifier.size(20.dp),
                                tint = CyanPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "App Theme",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Choose between Light, Dark, or System Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            title = "System",
                            icon = Icons.Default.Brightness4,
                            isSelected = currentThemeMode == ThemeMode.SYSTEM,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setThemeMode(ThemeMode.SYSTEM)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "Light",
                            icon = Icons.Default.Brightness7,
                            isSelected = currentThemeMode == ThemeMode.LIGHT,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setThemeMode(ThemeMode.LIGHT)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "Dark",
                            icon = Icons.Default.Brightness4,
                            isSelected = currentThemeMode == ThemeMode.DARK,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setThemeMode(ThemeMode.DARK)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Storage & Maintenance Section
            SectionTitle("Storage & Cache")
            SettingsOptionCard(
                icon = Icons.Default.CleaningServices,
                iconColor = CyanPrimary,
                title = "Clear Cache Files",
                subtitle = "App Cache: $cacheSizeStr • Frees temporary files",
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        clearAppCache(context)
                        val newSize = getCacheSize(context)
                        withContext(Dispatchers.Main) {
                            cacheSizeStr = newSize
                            snackbarHostState.showSnackbar("Temporary cache files cleared successfully!")
                        }
                    }
                }
            )

            // Privacy & Legal Section
            SectionTitle("Privacy & Terms")
            SettingsOptionCard(
                icon = Icons.Default.PrivacyTip,
                iconColor = EmeraldTertiary,
                title = "Privacy Policy",
                subtitle = "100% offline, zero data collection statement",
                onClick = onNavigatePrivacy
            )

            SettingsOptionCard(
                icon = Icons.Default.Description,
                iconColor = VioletSecondary,
                title = "Terms of Use",
                subtitle = "App usage guidelines & license",
                onClick = onNavigateTerms
            )

            SettingsOptionCard(
                icon = Icons.Default.Info,
                iconColor = CyanPrimary,
                title = "About Free Tools",
                subtitle = "Learn about offline local utility capabilities",
                onClick = onNavigateAbout
            )

            // Support & Feedback Section
            SectionTitle("Feedback & Share")
            SettingsOptionCard(
                icon = Icons.Default.Star,
                iconColor = Color(0xFFFFB74D),
                title = "Rate on Play Store",
                subtitle = "Support Free Tools by giving a review",
                onClick = {
                    val packageName = context.packageName
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                }
            )

            SettingsOptionCard(
                icon = Icons.Default.Share,
                iconColor = CyanPrimary,
                title = "Share Free Tools",
                subtitle = "Share offline utility tools with friends",
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out Free Tools: 100% Free & Offline Utility Tools (PDF Scanner, QR Scanner, Video Compressor & Image Tools)!")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share MuftTools")
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(shareIntent)
                }
            )

            SettingsOptionCard(
                icon = Icons.Default.Email,
                iconColor = VioletSecondary,
                title = "Contact Feedback Support",
                subtitle = "Send bug reports or suggestions via email",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:t84787704@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Free Tools Feedback & Support")
                    }
                    try {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar("No email app found. Email: t84787704@gmail.com")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Free Tools • Version 1.0.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "Built with Jetpack Compose & ML Kit",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsOptionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(22.dp),
                        tint = iconColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = TextMuted
            )
        }
    }
}

private fun getCacheSize(context: Context): String {
    return try {
        var size: Long = 0
        context.cacheDir.listFiles()?.forEach {
            size += getFolderSize(it)
        }
        FileUtil.getFileSizeString(size)
    } catch (e: Exception) {
        "0 KB"
    }
}

private fun getFolderSize(file: File): Long {
    var size: Long = 0
    if (file.isDirectory) {
        file.listFiles()?.forEach {
            size += getFolderSize(it)
        }
    } else {
        size = file.length()
    }
    return size
}

private fun clearAppCache(context: Context) {
    try {
        context.cacheDir.listFiles()?.forEach {
            it.deleteRecursively()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
private fun ThemeOptionChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else DarkSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isSelected) CyanPrimary else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) CyanPrimary else TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CyanPrimary else TextPrimary
            )
        }
    }
}
