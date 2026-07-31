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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
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
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.data.entity.pageList
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.utils.ExportManager
import com.example.documentscanner.utils.ExportResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewScreen(
    document: ScannedDocument,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var previewIndex by remember { mutableIntStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(document.fileName) { mutableStateOf(document.fileName) }
    var ocrEditText by remember(document.extractedText) { mutableStateOf(document.extractedText) }
    var isEditingOcr by remember { mutableStateOf(false) }

    val pages = remember(document) { document.pageList() }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DocVaultColors.CardSurface,
                    contentColor = DocVaultColors.TextPrimary,
                    actionColor = DocVaultColors.ElectricIndigo
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        document.fileName,
                        maxLines = 1,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DocVaultColors.TextPrimary,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                            tint = DocVaultColors.TextPrimary)
                    }
                },
                actions = {
                    // Share
                    IconButton(onClick = {
                        scope.launch {
                            val result = ExportManager.exportPdf(context, document, includeText = false)
                            if (result is ExportResult.Success) {
                                ExportManager.share(context, result.uri, "application/pdf")
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share",
                            tint = DocVaultColors.TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    // Rename
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename",
                            tint = DocVaultColors.TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    // Lock (placeholder for future per-doc lock)
                    IconButton(onClick = { /* future: per-doc lock */ }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock",
                            tint = DocVaultColors.TextSecondary, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DocVaultColors.DarkBackground
                )
            )
        },
        bottomBar = {
            // ── Bottom Export Bar ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DocVaultColors.CardSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Export PDF — Teal fill
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DocVaultColors.EmeraldVerified,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (isExporting) "Exporting..." else "Export PDF",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Export TXT — Outlined indigo border
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isExporting = true
                            val result = ExportManager.exportTxt(context, document)
                            isExporting = false
                            when (result) {
                                is ExportResult.Success -> {
                                    val action = snackbarHostState.showSnackbar(
                                        message = "Saved to Downloads/DocVault",
                                        actionLabel = "Share",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (action == SnackbarResult.ActionPerformed) {
                                        ExportManager.share(context, result.uri, "text/plain")
                                    }
                                }
                                is ExportResult.Error -> {
                                    snackbarHostState.showSnackbar("Export failed: ${result.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = document.extractedText.isNotEmpty() && !isExporting,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, DocVaultColors.ElectricIndigo
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DocVaultColors.ElectricIndigo,
                        disabledContentColor = DocVaultColors.TextTertiary
                    )
                ) {
                    Text("Export TXT", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DocVaultColors.DarkBackground)
        ) {
            // ── Tab Row ──────────────────────────────────────────
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DocVaultColors.DarkBackground,
                contentColor = DocVaultColors.ElectricIndigo,


            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Document View",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == 0) DocVaultColors.ElectricIndigo
                            else DocVaultColors.TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Text View",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == 1) DocVaultColors.ElectricIndigo
                            else DocVaultColors.TextSecondary
                        )
                    }
                )
            }

            // ── Tab Content ──────────────────────────────────────
            when (selectedTab) {
                0 -> DocumentViewTab(pages, previewIndex, onPageSelect = { previewIndex = it })
                1 -> TextViewTab(
                    extractedText = ocrEditText,
                    isEditing = isEditingOcr,
                    onEditToggle = { isEditingOcr = !isEditingOcr },
                    onTextChange = { ocrEditText = it }
                )
            }
        }
    }

    // ── Rename Dialog ────────────────────────────────────────────
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename document", color = DocVaultColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DocVaultColors.ElectricIndigo,
                        unfocusedBorderColor = DocVaultColors.Border,
                        focusedTextColor = DocVaultColors.TextPrimary,
                        unfocusedTextColor = DocVaultColors.TextPrimary,
                        cursorColor = DocVaultColors.ElectricIndigo,
                        focusedContainerColor = DocVaultColors.CardSurface,
                        unfocusedContainerColor = DocVaultColors.CardSurface
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                }) { Text("Save", color = DocVaultColors.ElectricIndigo) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    // ── PDF Export Dialog ────────────────────────────────────────
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export PDF", color = DocVaultColors.TextPrimary) },
            text = {
                Text(
                    "Include extracted text as a final page?",
                    color = DocVaultColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    scope.launch {
                        isExporting = true
                        val result = ExportManager.exportPdf(context, document, includeText = true)
                        isExporting = false
                        when (result) {
                            is ExportResult.Success -> {
                                val action = snackbarHostState.showSnackbar(
                                    message = "Saved to Downloads/DocVault",
                                    actionLabel = "Share",
                                    duration = SnackbarDuration.Long
                                )
                                if (action == SnackbarResult.ActionPerformed) {
                                    ExportManager.share(context, result.uri, "application/pdf")
                                }
                            }
                            is ExportResult.Error ->
                                snackbarHostState.showSnackbar("Export failed: ${result.message}")
                        }
                    }
                }) { Text("Image + Text", color = DocVaultColors.ElectricIndigo) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    scope.launch {
                        isExporting = true
                        val result = ExportManager.exportPdf(context, document, includeText = false)
                        isExporting = false
                        when (result) {
                            is ExportResult.Success -> {
                                val action = snackbarHostState.showSnackbar(
                                    message = "Saved to Downloads/DocVault",
                                    actionLabel = "Share",
                                    duration = SnackbarDuration.Long
                                )
                                if (action == SnackbarResult.ActionPerformed) {
                                    ExportManager.share(context, result.uri, "application/pdf")
                                }
                            }
                            is ExportResult.Error ->
                                snackbarHostState.showSnackbar("Export failed: ${result.message}")
                        }
                    }
                }) { Text("Image Only", color = DocVaultColors.TextSecondary) }
            }
        )
    }
}

// ── Document View Tab ────────────────────────────────────────────

@Composable
private fun DocumentViewTab(
    pages: List<String>,
    selectedIndex: Int,
    onPageSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Main image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(DocVaultColors.CardSurface)
                .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (pages.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse("file://${pages[selectedIndex]}")),
                    contentDescription = "Document page",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("No image available", color = DocVaultColors.TextTertiary)
            }
        }

        // Page thumbnail carousel
        if (pages.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pages.size) { index ->
                    val isSelected = index == selectedIndex
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
                            .clickable { onPageSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = Uri.parse("file://${pages[index]}"),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

// ── Text View Tab ────────────────────────────────────────────────

@Composable
private fun TextViewTab(
    extractedText: String,
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    onTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Edit OCR Text header button
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
            TextButton(onClick = onEditToggle) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = DocVaultColors.ElectricIndigo
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (isEditing) "Done" else "Edit OCR Text",
                    fontSize = 13.sp,
                    color = DocVaultColors.ElectricIndigo
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Text container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(DocVaultColors.CardSurface)
                .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            if (extractedText.isEmpty()) {
                Text(
                    "No text extracted yet",
                    color = DocVaultColors.TextTertiary,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (isEditing) {
                TextField(
                    value = extractedText,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 21.sp,         // 1.5 × 14sp
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
                SelectionContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = extractedText,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 21.sp,     // 1.5 × 14sp
                            color = DocVaultColors.TextPrimary
                        )
                    )
                }
            }
        }
    }
}
