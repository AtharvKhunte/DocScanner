@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.components.GlassmorphicCard
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.utils.ExportManager
import com.example.documentscanner.utils.ExportedFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var exports by remember { mutableStateOf(listOf<ExportedFile>()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load exports on first composition
    LaunchedEffect(Unit) {
        exports = ExportManager.listExports(context)
        isLoading = false
    }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Exports (${exports.size})") },
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
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DocVaultColors.ElectricIndigo)
                    }
                }

                exports.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No exports yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DocVaultColors.TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Exported PDFs and text files will appear here",
                                color = DocVaultColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(exports, key = { it.uri.toString() }) { export ->
                            ExportedFileCard(
                                file = export,
                                onShare = {
                                    ExportManager.share(context, export.uri, export.mimeType)
                                },
                                onDelete = {
                                    ExportManager.deleteExport(context, export.uri)
                                    exports = exports.filter { it.uri != export.uri }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("NonObservableLocale")
@Composable
private fun ExportedFileCard(
    file: ExportedFile,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        .format(Date(file.dateAdded))

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete export?", color = DocVaultColors.TextPrimary) },
            text = { Text("This removes the file from Downloads/DocVault. Your original document in the vault is not affected.", color = DocVaultColors.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Delete", color = DocVaultColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShare() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File type icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (file.ispdf) DocVaultColors.ElectricIndigo.copy(alpha = 0.15f)
                        else DocVaultColors.EmeraldVerified.copy(alpha = 0.15f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (file.ispdf) Icons.Default.PictureAsPdf else Icons.Default.TextSnippet,
                    contentDescription = null,
                    tint = if (file.ispdf) DocVaultColors.ElectricIndigo else DocVaultColors.EmeraldVerified,
                    modifier = Modifier.size(22.dp)
                )
            }

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    color = DocVaultColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "$dateStr · ${file.formattedSize}",
                    color = DocVaultColors.TextTertiary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Tap to share",
                    color = DocVaultColors.ElectricIndigo,
                    fontSize = 11.sp
                )
            }

            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DocVaultColors.Error)
            }
        }
    }
}