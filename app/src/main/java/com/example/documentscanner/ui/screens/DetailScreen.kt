package com.example.documentscanner.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.ui.viewmodel.DocumentViewModel
import com.example.documentscanner.ui.viewmodel.DocumentViewModelFactory
import com.example.documentscanner.ui.viewmodel.OCRState
import com.example.documentscanner.ui.viewmodel.SaveState
import java.io.File

@ExperimentalMaterial3Api
@Composable
fun DetailScreen(
    imagePath: String,
    onSave: () -> Unit,
    onRetake: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DocumentViewModel = viewModel(factory = DocumentViewModelFactory(context))
    val ocrState by viewModel.ocrState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Document Details") },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) }
                    IconButton(onClick = { }) { Icon(Icons.Default.Delete, "Delete", tint = DocVaultColors.Error) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DocVaultColors.DarkBackground,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(DocVaultColors.DarkBackground)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DocVaultColors.DarkGlassAlpha,
                contentColor = DocVaultColors.ElectricIndigo
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Document") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Extracted Text") })
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (selectedTab == 0) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse("file://$imagePath")),
                        contentDescription = "Document",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    ExtractedTextTab(ocrState, extractedText)
                }
            }

            ActionButtonsRow(
                ocrState = ocrState,
                saveState = saveState,
                extractedText = extractedText,
                onRetake = onRetake,
                onExtract = { viewModel.extractTextFromImage(imagePath) },
                onSave = {
                    val fileName = File(imagePath).name
                    viewModel.saveDocument(fileName, imagePath, extractedText)
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ onSave() }, 800)
                }
            )
        }
    }
}

@Composable
private fun ExtractedTextTab(ocrState: OCRState, extractedText: String) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (ocrState) {
            is OCRState.Idle -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No text extracted yet", color = DocVaultColors.TextSecondary, textAlign = TextAlign.Center)
            }
            is OCRState.Processing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = DocVaultColors.ElectricIndigo)
                    Spacer(Modifier.height(12.dp))
                    Text("Extracting text...", color = DocVaultColors.TextSecondary)
                }
            }
            is OCRState.Success -> SelectionContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DocVaultColors.WhiteGlassAlpha, RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(extractedText, color = DocVaultColors.TextPrimary)
            }
            is OCRState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${ocrState.message}", color = DocVaultColors.Error, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    ocrState: OCRState,
    saveState: SaveState,
    extractedText: String,
    onRetake: () -> Unit,
    onExtract: () -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onRetake,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.WhiteGlassAlpha, contentColor = Color.White),
                enabled = ocrState !is OCRState.Processing
            ) { Text("Retake") }

            Button(
                onClick = onExtract,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.ElectricIndigo),
                enabled = ocrState !is OCRState.Processing
            ) { Text(if (ocrState is OCRState.Processing) "Extracting..." else "Extract") }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.EmeraldVerified),
                enabled = extractedText.isNotEmpty() && saveState !is SaveState.Saving
            ) { Text(if (saveState is SaveState.Saving) "Saving..." else "Save") }
        }

        when (saveState) {
            is SaveState.Success -> Text("✓ Saved to vault", color = DocVaultColors.Success, modifier = Modifier.padding(top = 6.dp))
            is SaveState.Error -> Text("Error: ${saveState.message}", color = DocVaultColors.Error, modifier = Modifier.padding(top = 6.dp))
            else -> {}
        }
    }
}