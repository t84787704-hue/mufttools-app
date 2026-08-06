package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AboutUsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageToolsScreen
import com.example.ui.screens.PdfScannerScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TermsScreen
import com.example.ui.screens.VideoCompressorScreen
import com.example.ui.theme.MuftToolsTheme
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.ThemeMode

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
            val themeMode by homeViewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MuftToolsTheme(darkTheme = isDarkTheme) {
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
                },
                onNavigateSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigatePrivacy = { navController.navigate("privacy_policy") },
                onNavigateTerms = { navController.navigate("terms_of_use") },
                onNavigateAbout = { navController.navigate("about_us") }
            )
        }

        composable("privacy_policy") {
            PrivacyPolicyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("terms_of_use") {
            TermsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("about_us") {
            AboutUsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("pdf_scanner") {
            PdfScannerScreen(
                isFavorite = favoriteIds.contains("pdf_scanner"),
                onToggleFavorite = { viewModel.toggleFavorite("pdf_scanner") },
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
