@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasCameraPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission.value = isGranted
    }

    val imageCaptureRef = remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission.value) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Document") }
            )
        }
    ) { paddingValues ->
        if (hasCameraPermission.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    lifecycleOwner = lifecycleOwner,
                    imageCaptureRef = imageCaptureRef
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            android.util.Log.d("CameraScreen", "Capture button clicked")
                            capturePhoto(context, imageCaptureRef.value) { imagePath ->
                                android.util.Log.d("CameraScreen", "Calling onPhotoCaptured with: $imagePath")
                                onPhotoCaptured(imagePath)
                            }
                        }
                    ) {
                        Text("Capture")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission denied")
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    imageCaptureRef: androidx.compose.runtime.MutableState<ImageCapture?>
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    imageCaptureRef.value = imageCapture

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    android.util.Log.d("CameraPreview", "Camera initialized successfully")
                } catch (exc: Exception) {
                    android.util.Log.e("CameraPreview", "Camera initialization error: ${exc.message}", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onPhotoCaptured: (String) -> Unit
) {
    android.util.Log.d("CapturePhoto", "capturePhoto called")

    if (imageCapture == null) {
        android.util.Log.e("CapturePhoto", "ImageCapture is null!")
        Toast.makeText(context, "Camera error: ImageCapture is null", Toast.LENGTH_SHORT).show()
        return
    }

    val photoDir = File(context.getExternalFilesDir(null), "photos")
    android.util.Log.d("CapturePhoto", "Photo directory: ${photoDir.absolutePath}")

    if (!photoDir.exists()) {
        val created = photoDir.mkdirs()
        android.util.Log.d("CapturePhoto", "Directory created: $created")
    }

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val photoFile = File(photoDir, "photo_$timeStamp.jpg")

    android.util.Log.d("CapturePhoto", "Photo file path: ${photoFile.absolutePath}")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        Executors.newSingleThreadExecutor(),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                android.util.Log.d("CapturePhoto", "Image saved callback triggered")

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        // Verify file exists and has content
                        val fileExists = photoFile.exists()
                        val fileSize = if (fileExists) photoFile.length() else 0
                        val filePath = photoFile.absolutePath

                        android.util.Log.d("CapturePhoto", "File exists: $fileExists")
                        android.util.Log.d("CapturePhoto", "File size: $fileSize bytes")
                        android.util.Log.d("CapturePhoto", "File path: $filePath")

                        // Show Toast
                        Toast.makeText(
                            context,
                            "✓ Photo saved!\nSize: ${fileSize / 1024}KB",
                            Toast.LENGTH_LONG
                        ).show()

                        // Call the callback
                        android.util.Log.d("CapturePhoto", "Calling onPhotoCaptured callback")
                        onPhotoCaptured(filePath)
                        android.util.Log.d("CapturePhoto", "Callback called successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("CapturePhoto", "Error in onImageSaved: ${e.message}", e)
                        Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                android.util.Log.e("CapturePhoto", "Capture failed: ${exception.message}", exception)

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "✗ Capture failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )
}