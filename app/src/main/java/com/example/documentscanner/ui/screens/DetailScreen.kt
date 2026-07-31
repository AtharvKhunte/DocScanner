package com.example.documentscanner.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.ui.viewmodel.DocumentViewModel
import com.example.documentscanner.ui.viewmodel.DocumentViewModelFactory
import com.example.documentscanner.ui.viewmodel.OCRState
import com.example.documentscanner.ui.viewmodel.SaveState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    imagePaths: List<String>,
    onSave: () -> Unit,
    onRetake: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DocumentViewModel = viewModel(factory = DocumentViewModelFactory(context))
    val ocrState by viewModel.ocrState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val extractedText by viewModel.extractedText.collectAsState()

    var previewIndex by remember { mutableIntStateOf(0) }
    var isEditingOcr by remember { mutableStateOf(false) }
    var editedOcrText by remember(extractedText) { mutableStateOf(extractedText) }

    // Sync editedOcrText when OCR completes
    LaunchedEffect(extractedText) {
        editedOcrText = extractedText
    }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (imagePaths.isNotEmpty())
                            File(imagePaths.first()).name
                        else "New Document",
                        maxLines = 1,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DocVaultColors.TextPrimary,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onRetake) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DocVaultColors.TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { isEditingOcr = !isEditingOcr },
                        enabled = ocrState is OCRState.Success
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (ocrState is OCRState.Success)
                                DocVaultColors.ElectricIndigo
                            else DocVaultColors.TextTertiary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isEditingOcr) "Done" else "Edit OCR Text",
                            fontSize = 13.sp,
                            color = if (ocrState is OCRState.Success)
                                DocVaultColors.ElectricIndigo
                            else DocVaultColors.TextTertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DocVaultColors.DarkBackground
                )
            )
        },
        bottomBar = {
            // ── Bottom Action Bar ────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DocVaultColors.CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Status messages
                when (saveState) {
                    is SaveState.Success -> Text(
                        "✓ Saved to vault",
                        color = DocVaultColors.Success,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    is SaveState.Error -> Text(
                        "Error: ${(saveState as SaveState.Error).message}",
                        color = DocVaultColors.Error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    is SaveState.Saving -> Text(
                        "Saving...",
                        color = DocVaultColors.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    else -> {}
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Extract button (shown while idle)
                    if (ocrState is OCRState.Idle || ocrState is OCRState.Error) {
                        OutlinedButton(
                            onClick = { viewModel.extractTextFromImages(imagePaths) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, DocVaultColors.ElectricIndigo
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DocVaultColors.ElectricIndigo
                            )
                        ) {
                            Text("Extract Text", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Save to Vault — Teal
                    Button(
                        onClick = {
                            val fileName = File(imagePaths.first()).name
                            val textToSave = if (editedOcrText.isNotEmpty()) editedOcrText else extractedText
                            viewModel.saveDocument(fileName, imagePaths, textToSave)
                            android.os.Handler(android.os.Looper.getMainLooper())
                                .postDelayed({ onSave() }, 800)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = saveState !is SaveState.Saving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DocVaultColors.EmeraldVerified,
                            contentColor = Color.White,
                            disabledContainerColor = DocVaultColors.CardSurface
                        )
                    ) {
                        Text(
                            when (saveState) {
                                is SaveState.Saving -> "Saving..."
                                else -> "Save to Vault"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DocVaultColors.DarkBackground)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Image Preview ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DocVaultColors.CardSurface)
                    .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imagePaths.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            Uri.parse("file://${imagePaths[previewIndex]}")
                        ),
                        contentDescription = "Scanned document",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // ── Page Thumbnail Carousel ───────────────────────────
            if (imagePaths.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imagePaths.size) { index ->
                        val isSelected = index == previewIndex
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) DocVaultColors.ElectricIndigo
                                    else DocVaultColors.Border,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { previewIndex = index }
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    Uri.parse("file://${imagePaths[index]}")
                                ),
                                contentDescription = "Page ${index + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── OCR Text Section ─────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Section header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Extracted Text",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DocVaultColors.TextSecondary
                    )
                    if (imagePaths.size > 1) {
                        Text(
                            "${imagePaths.size} pages",
                            fontSize = 12.sp,
                            color = DocVaultColors.TextTertiary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // OCR Content Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DocVaultColors.CardSurface)
                        .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    when (ocrState) {
                        is OCRState.Idle -> {
                            Text(
                                "Tap \"Extract Text\" to scan this document",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    color = DocVaultColors.TextTertiary
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is OCRState.Processing -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = DocVaultColors.ElectricIndigo,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Extracting text...",
                                    fontSize = 13.sp,
                                    color = DocVaultColors.TextSecondary
                                )
                            }
                        }

                        is OCRState.Success -> {
                            if (isEditingOcr) {
                                TextField(
                                    value = editedOcrText,
                                    onValueChange = { editedOcrText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 21.sp,
                                        color = DocVaultColors.TextPrimary
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = DocVaultColors.ElectricIndigo,
                                        focusedTextColor = DocVaultColors.TextPrimary,
                                        unfocusedTextColor = DocVaultColors.TextPrimary
                                    )
                                )
                            } else {
                                SelectionContainer {
                                    Text(
                                        text = editedOcrText.ifEmpty { extractedText },
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            lineHeight = 21.sp,
                                            color = DocVaultColors.TextPrimary
                                        )
                                    )
                                }
                            }
                        }

                        is OCRState.Error -> {
                            Text(
                                "Error: ${(ocrState as OCRState.Error).message}",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    color = DocVaultColors.Error
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}