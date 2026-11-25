@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.data.entity.pageList
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.ui.viewmodel.DocumentListViewModel
import com.example.documentscanner.ui.viewmodel.DocumentListViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


enum class SortOrder { DATE_NEWEST, DATE_OLDEST, NAME_AZ, NAME_ZA }

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
            SortOrder.NAME_AZ     -> documents.sortedBy { it.fileName.lowercase() }
            SortOrder.NAME_ZA     -> documents.sortedByDescending { it.fileName.lowercase() }
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
                title = {
                    Text(
                        "My Vault (${documents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DocVaultColors.TextPrimary
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, "Sort", tint = DocVaultColors.TextSecondary)
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
                    containerColor = DocVaultColors.DarkBackground
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search Docs...", color = DocVaultColors.TextTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = DocVaultColors.TextTertiary, modifier = Modifier.size(18.dp))
                },
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

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DocVaultColors.ElectricIndigo)
                    }
                    documents.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No documents yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DocVaultColors.TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text("Scan your first document to get started", color = DocVaultColors.TextSecondary, textAlign = TextAlign.Center)
                        }
                    }
                    filteredDocuments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results for \"$searchQuery\"", color = DocVaultColors.TextTertiary)
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredDocuments, key = { it.id }) { doc ->
                            VaultDocumentCard(
                                document = doc,
                                onClick = { onDocumentClick(doc) },
                                onDelete = { viewModel.deleteDocument(doc.id) },
                                onRename = { newName -> viewModel.renameDocument(doc.id, newName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultDocumentCard(
    document: ScannedDocument,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    val dateStr = SimpleDateFormat(
        "MMM dd, yyyy",
        Locale.getDefault()
    ).format(Date(document.dateCreated))
    val firstPage = document.pageList().firstOrNull()
    val pages = document.pageList().size

    // Clean display name
    val displayName = if (document.fileName.matches(Regex("\\d+\\..*"))) {
        "Document · $dateStr"
    } else {
        document.fileName
    }

    // OCR snippet
    val snippet = document.extractedText
        .take(80)
        .replace("\n", " ")
        .ifEmpty { null }

    var showOverflowMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(document.fileName) { mutableStateOf(document.fileName) }

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DocVaultColors.CardSurface)
            .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DocVaultColors.DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            if (firstPage != null) {
                AsyncImage(
                    model = Uri.parse("file://$firstPage"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("📄", fontSize = 24.sp)
            }
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DocVaultColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                "$dateStr · $pages ${if (pages == 1) "page" else "pages"}",
                fontSize = 11.sp,
                color = DocVaultColors.TextSecondary
            )
            if (snippet != null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    snippet,
                    fontSize = 11.sp,
                    color = DocVaultColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Overflow menu
        Box {
            IconButton(onClick = { showOverflowMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.MoreVert, null, tint = DocVaultColors.TextSecondary, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Edit", color = DocVaultColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = DocVaultColors.TextSecondary, modifier = Modifier.size(16.dp)) },
                    onClick = { showOverflowMenu = false; showRenameDialog = true }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = DocVaultColors.Error) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = DocVaultColors.Error, modifier = Modifier.size(16.dp)) },
                    onClick = { showOverflowMenu = false; onDelete() }
                )
            }
        }
    }
}

@Composable
private fun SortMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = if (selected) DocVaultColors.ElectricIndigo else DocVaultColors.TextPrimary) },
        onClick = onClick,
        trailingIcon = { if (selected) Text("✓", color = DocVaultColors.ElectricIndigo) }
    )
}