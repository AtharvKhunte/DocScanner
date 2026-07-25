@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.ui.components.GlassmorphicCard
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.ui.viewmodel.DocumentListViewModel
import com.example.documentscanner.ui.viewmodel.DocumentListViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortOrder {
    DATE_NEWEST, DATE_OLDEST, NAME_AZ, NAME_ZA
}

@Composable
fun DocumentListScreen(
    onDocumentClick: (ScannedDocument) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DocumentListViewModel = viewModel(factory = DocumentListViewModelFactory(context))
    val documents by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE_NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }

    val sortedDocuments = remember(documents, sortOrder) {
        when (sortOrder) {
            SortOrder.DATE_NEWEST -> documents.sortedByDescending { it.dateCreated }
            SortOrder.DATE_OLDEST -> documents.sortedBy { it.dateCreated }
            SortOrder.NAME_AZ -> documents.sortedBy { it.fileName.lowercase() }
            SortOrder.NAME_ZA -> documents.sortedByDescending { it.fileName.lowercase() }
        }
    }

    val filteredDocuments = remember(sortedDocuments, searchQuery) {
        if (searchQuery.isBlank()) sortedDocuments
        else sortedDocuments.filter {
            it.fileName.contains(searchQuery, ignoreCase = true) ||
                    it.extractedText.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("My Vault (${documents.size})") },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortMenuItem("Newest first", sortOrder == SortOrder.DATE_NEWEST) {
                                sortOrder = SortOrder.DATE_NEWEST; showSortMenu = false
                            }
                            SortMenuItem("Oldest first", sortOrder == SortOrder.DATE_OLDEST) {
                                sortOrder = SortOrder.DATE_OLDEST; showSortMenu = false
                            }
                            SortMenuItem("Name A–Z", sortOrder == SortOrder.NAME_AZ) {
                                sortOrder = SortOrder.NAME_AZ; showSortMenu = false
                            }
                            SortMenuItem("Name Z–A", sortOrder == SortOrder.NAME_ZA) {
                                sortOrder = SortOrder.NAME_ZA; showSortMenu = false
                            }
                        }
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
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                placeholder = { Text("Search vault...", color = DocVaultColors.TextTertiary) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DocVaultColors.WhiteGlassAlpha,
                    unfocusedContainerColor = DocVaultColors.WhiteGlassAlpha,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = DocVaultColors.TextPrimary,
                    unfocusedTextColor = DocVaultColors.TextPrimary
                )
            )

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DocVaultColors.ElectricIndigo)
                    }
                    documents.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No documents yet", color = DocVaultColors.TextSecondary, textAlign = TextAlign.Center)
                    }
                    filteredDocuments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matches for \"$searchQuery\"", color = DocVaultColors.TextTertiary)
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDocuments, key = { it.id }) { doc ->
                            DocListCard(
                                document = doc,
                                onClick = { onDocumentClick(doc) },
                                onDelete = { viewModel.deleteDocument(doc.id) },
                                onRename = { newName -> viewModel.renameDocument(doc.id, newName) }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.ElectricIndigo)
            ) { Text("Back to Home") }
        }
    }
}

@Composable
private fun SortMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                label,
                color = if (selected) DocVaultColors.ElectricIndigo else DocVaultColors.TextPrimary
            )
        },
        onClick = onClick,
        trailingIcon = {
            if (selected) Text("✓", color = DocVaultColors.ElectricIndigo)
        }
    )
}

@SuppressLint("NonObservableLocale")
@Composable
private fun DocListCard(
    document: ScannedDocument,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(document.dateCreated))
    val preview = document.extractedText.take(100).replace("\n", " ").ifEmpty { "(No text extracted)" }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(document.fileName) { mutableStateOf(document.fileName) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename document", color = DocVaultColors.TextPrimary) },
            text = {
                TextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DocVaultColors.WhiteGlassAlpha,
                        unfocusedContainerColor = DocVaultColors.WhiteGlassAlpha,
                        focusedIndicatorColor = DocVaultColors.ElectricIndigo,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = DocVaultColors.TextPrimary,
                        unfocusedTextColor = DocVaultColors.TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) onRename(renameText.trim())
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

    GlassmorphicCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(document.fileName, color = DocVaultColors.TextPrimary, fontSize = 14.sp, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(dateStr, color = DocVaultColors.TextTertiary, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Text(preview, color = DocVaultColors.TextSecondary, fontSize = 12.sp, maxLines = 2)
            }

            Row {
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = DocVaultColors.TextSecondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DocVaultColors.Error)
                }
            }
        }
    }
}