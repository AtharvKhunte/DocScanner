package com.example.documentscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.utils.ExportManager
import com.example.documentscanner.utils.ExportedFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ExportFilter { ALL, PDF, TXT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var exports by remember { mutableStateOf(listOf<ExportedFile>()) }
    var isLoading by remember { mutableStateOf(true) }
    var activeFilter by remember { mutableStateOf(ExportFilter.ALL) }
    var showDeleteDialog by remember { mutableStateOf<ExportedFile?>(null) }

    LaunchedEffect(Unit) {
        exports = ExportManager.listExports(context)
        isLoading = false
    }

    val filteredExports = remember(exports, activeFilter) {
        when (activeFilter) {
            ExportFilter.ALL -> exports
            ExportFilter.PDF -> exports.filter { it.ispdf }
            ExportFilter.TXT -> exports.filter { !it.ispdf }
        }
    }

    showDeleteDialog?.let { fileToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete export?", color = DocVaultColors.TextPrimary) },
            text = {
                Text(
                    "Removes file from Downloads/DocVault. Vault document is not affected.",
                    color = DocVaultColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ExportManager.deleteExport(context, fileToDelete.uri)
                    exports = exports.filter { it.uri != fileToDelete.uri }
                    showDeleteDialog = null
                }) { Text("Delete", color = DocVaultColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Exports (${exports.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DocVaultColors.TextPrimary
                    )
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
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFilter.entries.forEach { filter ->
                    val selected = activeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (selected) DocVaultColors.ElectricIndigo
                                else DocVaultColors.CardSurface
                            )
                            .border(
                                1.dp,
                                if (selected) DocVaultColors.ElectricIndigo else DocVaultColors.Border,
                                RoundedCornerShape(50.dp)
                            )
                            .clickable { activeFilter = filter }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            filter.name,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) Color.White else DocVaultColors.TextSecondary
                        )
                    }
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DocVaultColors.ElectricIndigo)
                }

                exports.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No exports yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DocVaultColors.TextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Open a document and tap Export PDF or Export TXT",
                            color = DocVaultColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }

                filteredExports.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${activeFilter.name} exports yet",
                        color = DocVaultColors.TextTertiary,
                        fontSize = 14.sp
                    )
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExports, key = { it.uri.toString() }) { file ->
                        ExportFileCard(
                            file = file,
                            onShare = { ExportManager.share(context, file.uri, file.mimeType) },
                            onDelete = { showDeleteDialog = file }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportFileCard(
    file: ExportedFile,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        .format(Date(file.dateAdded))

    val isPdf = file.ispdf
    val badgeColor = if (isPdf) Color(0xFFEF4444) else Color(0xFF10B981)
    val badgeLabel = if (isPdf) "PDF" else "TXT"

    // Clean display name: strip timestamp suffix, replace underscores
    val cleanName = file.name
        .substringBeforeLast(".")
        .replace(Regex("_\\d{8}_\\d{6}$"), "")
        .replace("_", " ")
        .trim()
        .ifEmpty { file.name }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DocVaultColors.CardSurface)
            .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Badge
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(badgeLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badgeColor)
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cleanName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DocVaultColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(file.formattedSize, fontSize = 12.sp, color = DocVaultColors.TextSecondary)
            Spacer(Modifier.height(2.dp))
            Text(dateStr, fontSize = 11.sp, color = DocVaultColors.TextTertiary)
        }

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Share, "Share", tint = DocVaultColors.TextSecondary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = DocVaultColors.Error, modifier = Modifier.size(18.dp))
            }
        }
    }
}