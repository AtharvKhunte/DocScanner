package com.example.documentscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.ui.screens.CameraScreen
import com.example.documentscanner.ui.screens.DetailScreen
import com.example.documentscanner.ui.screens.DocumentListScreen
import com.example.documentscanner.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Detail : Screen("detail")
    object DocumentList : Screen("document_list")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentImagePath = remember { mutableStateOf("") }
    val selectedDocument = remember { mutableStateOf<ScannedDocument?>(null) }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = {
                    android.util.Log.d("NavGraph", "Home: Navigating to Camera")
                    navController.navigate(Screen.Camera.route)
                },
                onViewDocumentsClick = {
                    android.util.Log.d("NavGraph", "Home: Navigating to DocumentList")
                    navController.navigate(Screen.DocumentList.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onPhotoCaptured = { imagePath ->
                    android.util.Log.d("NavGraph", "Camera: Photo captured at $imagePath")
                    currentImagePath.value = imagePath
                    navController.navigate(Screen.Detail.route)
                },
                onCancel = {
                    android.util.Log.d("NavGraph", "Camera: Cancel clicked")
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Detail.route) {
            DetailScreen(
                imagePath = currentImagePath.value,
                onSave = {
                    android.util.Log.d("NavGraph", "Detail: Save clicked")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetake = {
                    android.util.Log.d("NavGraph", "Detail: Retake clicked")
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DocumentList.route) {
            DocumentListScreen(
                onDocumentClick = { document ->
                    android.util.Log.d("NavGraph", "DocumentList: Document clicked - ${document.fileName}")
                    selectedDocument.value = document
                },
                onBack = {
                    android.util.Log.d("NavGraph", "DocumentList: Back clicked")
                    navController.popBackStack()
                }
            )
        }
    }
}