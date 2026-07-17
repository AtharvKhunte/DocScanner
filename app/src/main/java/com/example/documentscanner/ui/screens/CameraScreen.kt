package com.example.documentscanner.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.documentscanner.ui.theme.DocVaultColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import androidx.camera.core.ImageCaptureException

@ExperimentalMaterial3Api
@Composable
fun CameraScreen(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var flashEnabled by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Document") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DocVaultColors.DarkBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { ctx -> PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val capture = ImageCapture.Builder()
                            .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                            .build()
                        imageCapture = capture
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                        } catch (exc: Exception) {
                            android.util.Log.e("CameraScreen", "Binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )

            CameraOverlay()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { flashEnabled = !flashEnabled }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle flash",
                            tint = Color.White
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { capturePhoto(context, imageCapture, cameraExecutor, onPhotoCaptured) },
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.EmeraldVerified),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(modifier = Modifier.size(24.dp).background(Color.White, CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().drawBehind {
            val cornerSize = 40.dp.toPx()
            val strokeW = 3.dp.toPx()
            val inset = 20.dp.toPx()
            val color = DocVaultColors.EmeraldVerified

            drawLine(color, Offset(inset, inset), Offset(inset + cornerSize, inset), strokeW)
            drawLine(color, Offset(inset, inset), Offset(inset, inset + cornerSize), strokeW)
            drawLine(color, Offset(size.width - inset, inset), Offset(size.width - inset - cornerSize, inset), strokeW)
            drawLine(color, Offset(size.width - inset, inset), Offset(size.width - inset, inset + cornerSize), strokeW)
            drawLine(color, Offset(inset, size.height - inset), Offset(inset + cornerSize, size.height - inset), strokeW)
            drawLine(color, Offset(inset, size.height - inset), Offset(inset, size.height - inset - cornerSize), strokeW)
            drawLine(color, Offset(size.width - inset, size.height - inset), Offset(size.width - inset - cornerSize, size.height - inset), strokeW)
            drawLine(color, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - cornerSize), strokeW)
        }
    )
}

private fun capturePhoto(
    context: android.content.Context,
    imageCapture: ImageCapture?,
    executor: java.util.concurrent.Executor,
    onPhotoCaptured: (String) -> Unit
) {
    if (imageCapture == null) return
    val photoDir = File(context.getExternalFilesDir(null), "photos")
    if (!photoDir.exists()) photoDir.mkdirs()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(java.util.Date())
    val photoFile = File(photoDir, "IMG_$timeStamp.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onPhotoCaptured(photoFile.absolutePath)
            }
        }
        override fun onError(exc: ImageCaptureException) {
            android.util.Log.e("CameraScreen", "Capture failed: ${exc.message}", exc)
        }
    })
}