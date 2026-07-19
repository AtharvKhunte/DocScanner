package com.example.documentscanner.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.utils.DocumentScannerLauncher
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun CameraScreen(
    onScanComplete: (List<String>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            if (result != null) {
                val paths = DocumentScannerLauncher.extractImagePaths(result)
                if (paths.isNotEmpty()) {
                    onScanComplete(paths)
                } else {
                    onCancel()
                }
            } else {
                onCancel()
            }
        } else {
            // User cancelled the scan
            onCancel()
        }
    }

    LaunchedEffect(Unit) {
        if (activity == null) {
            onCancel()
            return@LaunchedEffect
        }
        val scanner = GmsDocumentScanning.getClient(DocumentScannerLauncher.scannerOptions)
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(
                    androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
                )
            }
            .addOnFailureListener {
                android.util.Log.e("CameraScreen", "Failed to start scanner: ${it.message}", it)
                onCancel()
            }
    }

    // Brief loading state while the scanner intent launches
    Scaffold(containerColor = DocVaultColors.DarkBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DocVaultColors.DarkBackground)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = DocVaultColors.ElectricIndigo)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Opening scanner...", color = DocVaultColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}