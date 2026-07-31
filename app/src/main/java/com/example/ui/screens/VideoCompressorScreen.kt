package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.data.ToolRepository
import com.example.ui.components.ToolDetailTemplate

@Composable
fun VideoCompressorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val tool = ToolRepository.defaultTools.first { it.id == "video_compressor" }
    ToolDetailTemplate(
        tool = tool,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBackClick = onBackClick
    )
}
