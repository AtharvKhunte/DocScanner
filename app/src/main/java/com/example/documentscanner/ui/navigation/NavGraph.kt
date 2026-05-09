package com.example.documentscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.documentscanner.ui.screens.CameraScreen
import com.example.documentscanner.ui.screens.DetailScreen
import com.example.documentscanner.ui.screens.HomeScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Detail : Screen("detail")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentImagePath = remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = {
                    android.util.Log.d("NavGraph", "Home: Navigating to Camera")
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onPhotoCaptured = { imagePath ->
                    android.util.Log.d("NavGraph", "Camera: Photo captured at $imagePath")
                    currentImagePath.value = imagePath
                    android.util.Log.d("NavGraph", "Camera: Navigating to Detail")
                    navController.navigate(Screen.Detail.route)
                },
                onCancel = {
                    android.util.Log.d("NavGraph", "Camera: Cancel clicked")
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Detail.route) {
            android.util.Log.d("NavGraph", "Detail: Displaying image: ${currentImagePath.value}")
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
    }
}