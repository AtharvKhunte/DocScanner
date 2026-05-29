@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.documentscanner.ui.viewmodel.DocumentViewModel
import com.example.documentscanner.ui.viewmodel.DocumentViewModelFactory
import com.example.documentscanner.ui.viewmodel.OCRState
import com.example.documentscanner.ui.viewmodel.SaveState
import java.io.File

@Composable
fun DetailScreen(
    imagePath: String,
    onSave: () -> Unit,
    onRetake: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DocumentViewModel = viewModel(
        factory = DocumentViewModelFactory(context)
    )

    val ocrState by viewModel.ocrState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Preview") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ===== IMAGE PREVIEW SECTION =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse("file://$imagePath")),
                    contentDescription = "Captured Document",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ===== EXTRACTED TEXT SECTION =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Extracted Text",
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show OCR State based on current state
                when (ocrState) {
                    is OCRState.Idle -> {
                        Text(
                            text = "👇 Tap 'Extract Text' to scan document",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    is OCRState.Processing -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Extracting text from image...",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    is OCRState.Success -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = extractedText,
                                fontSize = 12.sp
                            )
                        }
                    }

                    is OCRState.Error -> {
                        Text(
                            text = "❌ ${(ocrState as OCRState.Error).message}",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ===== SAVE STATUS MESSAGES =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (saveState) {
                    is SaveState.Error -> {
                        Text(
                            text = "❌ ${(saveState as SaveState.Error).message}",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    is SaveState.Success -> {
                        Text(
                            text = "✅ Document saved to database! (ID: ${(saveState as SaveState.Success).documentId})",
                            color = Color.Green,
                            fontSize = 12.sp
                        )
                    }

                    is SaveState.Saving -> {
                        Text(
                            text = "💾 Saving to database...",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp
                        )
                    }

                    else -> {} // Idle state - show nothing
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== ACTION BUTTONS =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Retake Button
                Button(
                    onClick = {
                        android.util.Log.d("DetailScreen", "Retake clicked")
                        onRetake()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake")
                }

                // Extract Text Button
                Button(
                    onClick = {
                        android.util.Log.d("DetailScreen", "Extract Text clicked - Image: $imagePath")
                        viewModel.extractTextFromImage(imagePath)
                    },
                    enabled = ocrState !is OCRState.Processing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (ocrState is OCRState.Processing) "Extracting..." else "Extract Text"
                    )
                }

                // Save Button
                Button(
                    onClick = {
                        if (extractedText.isNotEmpty()) {
                            android.util.Log.d("DetailScreen", "Save clicked with ${extractedText.length} characters")
                            val fileName = File(imagePath).name
                            viewModel.saveDocument(
                                fileName = fileName,
                                filePath = imagePath,
                                extractedText = extractedText
                            )
                            // Navigate after a short delay to show success message
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                onSave()
                            }, 800)
                        } else {
                            android.util.Log.w("DetailScreen", "Cannot save - no text extracted")
                        }
                    },
                    enabled = extractedText.isNotEmpty() && saveState !is SaveState.Saving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when (saveState) {
                            is SaveState.Saving -> "Saving..."
                            else -> "Save"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/*
DetailScreen - Week 2 Days 1-4 Features:

Layout:
1. Top: TopAppBar with title
2. Middle: Image preview (250dp height)
3. Middle: Extracted text section
4. Middle: Save status messages
5. Bottom: Action buttons (Retake, Extract, Save)

OCR States (Processing):
- Idle: Show hint to tap Extract
- Processing: Show loading spinner
- Success: Display extracted text
- Error: Show error message

Save States (Database):
- Idle: Show nothing
- Saving: Show "Saving..." message
- Success: Show "Saved with ID" message
- Error: Show error message

Buttons:
1. Retake: Go back to camera
   - Always enabled
   - Navigates back

2. Extract Text: Run OCR
   - Disabled while processing
   - Calls viewModel.extractTextFromImage()
   - Shows spinner while processing

3. Save: Save to database
   - Enabled only if text extracted
   - Disabled while saving
   - Calls viewModel.saveDocument()
   - Navigates after save

Full Workflow:
User captures photo
  ↓
DetailScreen shows photo
  ↓
User taps "Extract Text"
  ↓
ML Kit OCR processes image
  ↓
Extracted text displays
  ↓
User taps "Save"
  ↓
ViewModel saves to encrypted database
  ↓
Success message shows
  ↓
Navigate back to Home
*/