@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.ui.screens.CameraScreen
import com.example.documentscanner.ui.screens.DetailScreen
import com.example.documentscanner.ui.screens.DocumentListScreen
import com.example.documentscanner.ui.screens.DocumentViewScreen
import com.example.documentscanner.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Detail : Screen("detail")
    object DocumentList : Screen("document_list")
    object DocumentView : Screen("document_view")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentImagePath = remember { mutableStateOf("") }
    val selectedDocument = remember { mutableStateOf<ScannedDocument?>(null) }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = { navController.navigate(Screen.Camera.route) },
                onViewVaultClick = { navController.navigate(Screen.DocumentList.route) }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onPhotoCaptured = { imagePath ->
                    currentImagePath.value = imagePath
                    navController.navigate(Screen.Detail.route)
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.Detail.route) {
            DetailScreen(
                imagePath = currentImagePath.value,
                onSave = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetake = { navController.popBackStack() }
            )
        }

        composable(Screen.DocumentList.route) {
            DocumentListScreen(
                onDocumentClick = { document ->
                    selectedDocument.value = document
                    navController.navigate(Screen.DocumentView.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DocumentView.route) {
            selectedDocument.value?.let { document ->
                DocumentViewScreen(
                    document = document,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}