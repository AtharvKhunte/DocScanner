@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.data.entity.pageList
import com.example.documentscanner.ui.components.GlassmorphicCard
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.utils.PdfExporter
import com.example.documentscanner.utils.ShareUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("NonObservableLocale")
@Composable
fun DocumentViewScreen(
    document: ScannedDocument,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var previewIndex by remember { mutableIntStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(document.dateCreated))
    val pages = remember(document) { document.pageList() }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(document.fileName, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DocVaultColors.DarkBackground)
        ) {
            Text(
                if (pages.size > 1) "$dateStr · ${pages.size} pages" else dateStr,
                color = DocVaultColors.TextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(DocVaultColors.WhiteGlassAlpha, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                ViewTabButton("Document", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                ViewTabButton("Extracted Text", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
            ) {
                if (selectedTab == 0) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse("file://${pages[previewIndex]}")),
                                    contentDescription = "Document",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        if (pages.size > 1) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pages.size) { index ->
                                    val isSelected = index == previewIndex
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) DocVaultColors.ElectricIndigo
                                                else DocVaultColors.WhiteGlassAlpha
                                            )
                                            .clickable { previewIndex = index },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(Uri.parse("file://${pages[index]}")),
                                            contentDescription = "Page ${index + 1}",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (document.extractedText.isNotEmpty()) {
                        GlassmorphicCard(modifier = Modifier.fillMaxSize()) {
                            SelectionContainer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    document.extractedText,
                                    color = DocVaultColors.TextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No text extracted", color = DocVaultColors.TextSecondary, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.EmeraldVerified)
                ) { Text("Export PDF") }

                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.ElectricIndigo)
                ) { Text("Close") }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export PDF") },
            text = { Text("Include the extracted text as a final page?") },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    val pdfFile = PdfExporter.exportToPdf(context, document, includeText = true)
                    if (pdfFile != null) ShareUtils.sharePdf(context, pdfFile)
                }) { Text("Image + Text") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    val pdfFile = PdfExporter.exportToPdf(context, document, includeText = false)
                    if (pdfFile != null) ShareUtils.sharePdf(context, pdfFile)
                }) { Text("Image Only") }
            }
        )
    }
}

@Composable
private fun ViewTabButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) DocVaultColors.ElectricIndigo else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else DocVaultColors.TextTertiary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}