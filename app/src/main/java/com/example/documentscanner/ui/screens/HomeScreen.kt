package com.example.documentscanner.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.documentscanner.R
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.data.entity.pageList
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.ui.viewmodel.DocumentListViewModel
import com.example.documentscanner.ui.viewmodel.DocumentListViewModelFactory
import com.example.documentscanner.utils.ExportManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onViewVaultClick: () -> Unit,
    onViewExportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDocumentClick: (ScannedDocument) -> Unit
) {
    val context = LocalContext.current
    val viewModel: DocumentListViewModel = viewModel(
        factory = DocumentListViewModelFactory(context)
    )
    val documents by viewModel.documents.collectAsState()

    val totalDocs = documents.size
    val exports = remember(documents) { ExportManager.listExports(context) }
    val pdfCount = remember(exports) { exports.count { it.ispdf } }
    val storageBytes = remember(documents) {
        context.getExternalFilesDir(null)
            ?.walkTopDown()
            ?.filter { it.isFile }
            ?.sumOf { it.length() } ?: 0L
    }
    val todayCount = remember(documents) {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        documents.count { it.dateCreated >= startOfDay }
    }
    val recentDocs = remember(documents) {
        documents.sortedByDescending { it.dateCreated }.take(3)
    }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DocVault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DocVaultColors.TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DocVaultColors.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DocVaultColors.DarkBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DocVaultColors.DarkBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Hero ─────────────────────────────────────────────
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_svg),
                        contentDescription = "DocVault Logo",
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "DocVault",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = DocVaultColors.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "100% Offline. Encrypted. Private.",
                        fontSize = 13.sp,
                        color = DocVaultColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Scan Button ───────────────────────────────────────
            item {
                Button(
                    onClick = onScanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DocVaultColors.ElectricIndigo,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Scan Document",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Stats Grid ────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "OVERVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DocVaultColors.TextTertiary,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            value = totalDocs.toString(),
                            label = "Documents",
                            icon = Icons.Default.Description,
                            iconTint = DocVaultColors.ElectricIndigo
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            value = pdfCount.toString(),
                            label = "Exports",
                            icon = Icons.Default.Upload,
                            iconTint = DocVaultColors.EmeraldVerified
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            value = formatBytes(storageBytes),
                            label = "Storage Used",
                            icon = Icons.Default.Storage,
                            iconTint = DocVaultColors.ElectricIndigo
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            value = todayCount.toString(),
                            label = "Scanned Today",
                            icon = Icons.Default.CalendarToday,
                            iconTint = DocVaultColors.EmeraldVerified
                        )
                    }
                }
            }

            // ── Recent Documents ──────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "RECENT DOCUMENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DocVaultColors.TextTertiary,
                        letterSpacing = 1.sp
                    )
                    if (documents.isNotEmpty()) {
                        Text(
                            "See all",
                            fontSize = 12.sp,
                            color = DocVaultColors.ElectricIndigo,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onViewVaultClick() }
                        )
                    }
                }
            }

            if (recentDocs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DocVaultColors.CardSurface)
                            .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DocVaultColors.ElectricIndigo.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = DocVaultColors.ElectricIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No documents yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DocVaultColors.TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap Scan Document to get started",
                                fontSize = 12.sp,
                                color = DocVaultColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(recentDocs, key = { it.id }) { doc ->
                    RecentDocCard(
                        document = doc,
                        onClick = { onDocumentClick(doc) }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Stat Card ─────────────────────────────────────────────────────

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    iconTint: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DocVaultColors.CardSurface)
            .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DocVaultColors.TextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = DocVaultColors.TextSecondary
        )
    }
}

// ── Recent Doc Card ───────────────────────────────────────────────

@SuppressLint("NonObservableLocale")
@Composable
private fun RecentDocCard(
    document: ScannedDocument,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        .format(Date(document.dateCreated))
    val firstPage = document.pageList().firstOrNull()
    val preview = document.extractedText
        .take(60)
        .replace("\n", " ")
        .ifEmpty { "No text extracted" }

    // Clean up raw filenames like "712033264709723.jpg" → "Document · Aug 06"
    val displayName = if (document.fileName.matches(Regex("\\d+\\..*"))) {
        "Document · $dateStr"
    } else {
        document.fileName
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DocVaultColors.CardSurface)
            .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DocVaultColors.DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            if (firstPage != null) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse("file://$firstPage")),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = DocVaultColors.ElectricIndigo,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DocVaultColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                dateStr,
                fontSize = 11.sp,
                color = DocVaultColors.TextTertiary
            )
            Spacer(Modifier.height(3.dp))
            Text(
                preview,
                fontSize = 11.sp,
                color = DocVaultColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = DocVaultColors.TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${bytes / 1024}KB"
    else -> "${"%.1f".format(bytes / (1024f * 1024f))}MB"
}