package com.example.documentscanner.ui.navigation

import android.net.Uri // Required for encoding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost // Corrected import
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.documentscanner.ui.screens.CameraScreen
import com.example.documentscanner.ui.screens.DetailScreen
import com.example.documentscanner.ui.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Detail : Screen("detail/{imagePath}") {
        fun createRoute(imagePath: String) = "detail/${Uri.encode(imagePath)}"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val lastCapturedImage = remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = {
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onPhotoCaptured = { imagePath ->
                    lastCapturedImage.value = imagePath
                    navController.navigate(Screen.Detail.createRoute(imagePath))
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Detail.route) { backStackEntry ->
            val imagePath = Uri.decode(backStackEntry.arguments?.getString("imagePath") ?: "")
            DetailScreen(
                imagePath = imagePath,
                onSave = {
                    // For Week 1, just go back to home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetake = {
                    navController.popBackStack()
                }
            )
        }
    }
}