package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.BgRemoverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageToolsScreen
import com.example.ui.screens.PdfScannerScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.VideoCompressorScreen
import com.example.ui.theme.MuftToolsTheme
import com.example.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        enableEdgeToEdge()
        setContent {
            MuftToolsTheme {
                val navController = rememberNavController()
                MuftToolsNavGraph(
                    navController = navController,
                    viewModel = homeViewModel
                )
            }
        }
    }
}

@Composable
fun MuftToolsNavGraph(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onToolClick = { tool ->
                    navController.navigate(tool.route)
                }
            )
        }

        composable("pdf_scanner") {
            PdfScannerScreen(
                isFavorite = favoriteIds.contains("pdf_scanner"),
                onToggleFavorite = { viewModel.toggleFavorite("pdf_scanner") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("bg_remover") {
            BgRemoverScreen(
                isFavorite = favoriteIds.contains("bg_remover"),
                onToggleFavorite = { viewModel.toggleFavorite("bg_remover") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("qr_scanner") {
            QrScannerScreen(
                isFavorite = favoriteIds.contains("qr_scanner"),
                onToggleFavorite = { viewModel.toggleFavorite("qr_scanner") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("video_compressor") {
            VideoCompressorScreen(
                isFavorite = favoriteIds.contains("video_compressor"),
                onToggleFavorite = { viewModel.toggleFavorite("video_compressor") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("image_tools") {
            ImageToolsScreen(
                isFavorite = favoriteIds.contains("image_tools"),
                onToggleFavorite = { viewModel.toggleFavorite("image_tools") },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
