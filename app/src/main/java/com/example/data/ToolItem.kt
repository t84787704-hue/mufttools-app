package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ToolItem(
    val id: String,
    val title: String,
    val shortDescription: String,
    val detailedDescription: String,
    val category: String,
    val badge: String,
    val accentColor: Color,
    val icon: ImageVector,
    val route: String,
    val statusText: String,
    val features: List<String>
)