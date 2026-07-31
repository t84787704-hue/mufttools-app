package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.data.ToolRepository
import com.example.ui.components.ToolDetailTemplate

@Composable
fun QrScannerScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val tool = ToolRepository.defaultTools.first { it.id == "qr_scanner" }
    ToolDetailTemplate(
        tool = tool,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBackClick = onBackClick
    )
}
