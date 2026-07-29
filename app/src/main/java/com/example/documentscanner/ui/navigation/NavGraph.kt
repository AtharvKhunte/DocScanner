package com.example.documentscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.ui.screens.CameraScreen
import com.example.documentscanner.ui.screens.DetailScreen
import com.example.documentscanner.ui.screens.DocumentListScreen
import com.example.documentscanner.ui.screens.DocumentViewScreen
import com.example.documentscanner.ui.screens.ExportsScreen
import com.example.documentscanner.ui.screens.HomeScreen
import com.example.documentscanner.ui.screens.LockScreen
import com.example.documentscanner.ui.screens.SettingsScreen
import com.example.documentscanner.ui.screens.SetupScreen
import com.example.documentscanner.utils.AppLockManager
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Lock : Screen("lock")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Detail : Screen("detail")
    object DocumentList : Screen("document_list")
    object DocumentView : Screen("document_view")
    object Exports : Screen("exports")

    object Settings : Screen("settings")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val currentImagePaths = remember { mutableStateOf(listOf<String>()) }
    val selectedDocument = remember { mutableStateOf<ScannedDocument?>(null) }

    val startDestination = if (AppLockManager.isSetupComplete(context)) {
        Screen.Lock.route
    } else {
        Screen.Setup.route
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupComplete = {
                    AppLockManager.markSetupComplete(context)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Lock.route) {
            LockScreen(
                onUnlocked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = { navController.navigate(Screen.Camera.route) },
                onViewVaultClick = { navController.navigate(Screen.DocumentList.route) },
                onViewExportsClick = { navController.navigate(Screen.Exports.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onScanComplete = { paths ->
                    currentImagePaths.value = paths
                    navController.navigate(Screen.Detail.route)
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.Detail.route) {
            DetailScreen(
                imagePaths = currentImagePaths.value,
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

        composable(Screen.Exports.route) {
            ExportsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onDeleteAllDocuments = {
                    // Need a shared ViewModel or direct DB call here
                    val db = com.example.documentscanner.data.database.DocumentDatabase.getInstance(context)
                    val repo = com.example.documentscanner.domain.repository.DocumentRepository(db.documentDao())
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        repo.deleteAllDocuments()
                    }
                    navController.popBackStack()
                }
            )
        }

    }
}