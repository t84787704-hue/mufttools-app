package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ToolItem
import com.example.data.ToolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _favoriteIds = MutableStateFlow<Set<String>>(setOf("pdf_scanner"))
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    private val _selectedNavIndex = MutableStateFlow(0)
    val selectedNavIndex: StateFlow<Int> = _selectedNavIndex

    val allTools: List<ToolItem> = ToolRepository.defaultTools

    val filteredTools: StateFlow<List<ToolItem>> = combine(
        _searchQuery,
        _selectedCategory,
        _favoriteIds
    ) { query, category, favorites ->
        allTools.filter { tool ->
            val matchesQuery = query.isEmpty() ||
                    tool.title.contains(query, ignoreCase = true) ||
                    tool.shortDescription.contains(query, ignoreCase = true) ||
                    tool.category.contains(query, ignoreCase = true) ||
                    tool.badge.contains(query, ignoreCase = true)

            val matchesCategory = when (category) {
                "All" -> true
                "Favorites" -> favorites.contains(tool.id)
                else -> tool.category.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = allTools
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(toolId: String) {
        val current = _favoriteIds.value
        _favoriteIds.value = if (current.contains(toolId)) {
            current - toolId
        } else {
            current + toolId
        }
    }

    fun selectNavIndex(index: Int) {
        _selectedNavIndex.value = index
    }
}
