package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.data.ToolRepository
import com.example.ui.components.ToolDetailTemplate

@Composable
fun ImageToolsScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit
) {
    val tool = ToolRepository.defaultTools.first { it.id == "image_tools" }
    ToolDetailTemplate(
        tool = tool,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBackClick = onBackClick
    )
}
