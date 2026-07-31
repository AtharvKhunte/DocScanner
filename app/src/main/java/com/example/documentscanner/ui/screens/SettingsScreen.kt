package com.example.documentscanner.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.utils.ExportManager
import java.io.File

// ── Preference Keys (SharedPreferences) ──────────────────────────

private const val PREFS_SETTINGS = "docvault_settings"
private const val KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"
private const val KEY_AUTO_LOCK = "auto_lock_minutes"
private const val KEY_DEFAULT_EXPORT = "default_export_format"
private const val KEY_PDF_INCLUDE_TEXT = "pdf_include_text"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDeleteAllDocuments: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_SETTINGS, android.content.Context.MODE_PRIVATE)
    }

    // ── State ─────────────────────────────────────────────────────
    var biometricEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_BIOMETRIC_LOCK, true))
    }
    var autoLockMinutes by remember {
        mutableStateOf(prefs.getInt(KEY_AUTO_LOCK, 0))
    }
    var defaultExportFormat by remember {
        mutableStateOf(prefs.getString(KEY_DEFAULT_EXPORT, "PDF") ?: "PDF")
    }
    var pdfIncludeText by remember {
        mutableStateOf(prefs.getBoolean(KEY_PDF_INCLUDE_TEXT, true))
    }

    // Dialog states
    var showAutoLockDialog by remember { mutableStateOf(false) }
    var showExportFormatDialog by remember { mutableStateOf(false) }
    var showClearExportsDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Storage info
    val vaultSizeStr = remember {
        val dir = context.getExternalFilesDir(null)
        val bytes = dir?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
        formatBytes(bytes)
    }
    val exportSizeStr = remember {
        val exports = ExportManager.listExports(context)
        val bytes = exports.sumOf { it.sizeBytes }
        formatBytes(bytes)
    }

    val canUseBiometric = remember {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // ── Dialogs ───────────────────────────────────────────────────

    if (showAutoLockDialog) {
        val options = listOf(0 to "Immediately", 1 to "After 1 minute", 5 to "After 5 minutes", -1 to "Never")
        AlertDialog(
            onDismissRequest = { showAutoLockDialog = false },
            title = { Text("Auto-lock timer", color = DocVaultColors.TextPrimary) },
            text = {
                Column {
                    options.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = autoLockMinutes == minutes,
                                onClick = {
                                    autoLockMinutes = minutes
                                    prefs.edit().putInt(KEY_AUTO_LOCK, minutes).apply()
                                    showAutoLockDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DocVaultColors.ElectricIndigo
                                )
                            )
                            Text(label, color = DocVaultColors.TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoLockDialog = false }) {
                    Text("Close", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    if (showExportFormatDialog) {
        val options = listOf("PDF", "TXT")
        AlertDialog(
            onDismissRequest = { showExportFormatDialog = false },
            title = { Text("Default export format", color = DocVaultColors.TextPrimary) },
            text = {
                Column {
                    options.forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = defaultExportFormat == format,
                                onClick = {
                                    defaultExportFormat = format
                                    prefs.edit().putString(KEY_DEFAULT_EXPORT, format).apply()
                                    showExportFormatDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DocVaultColors.ElectricIndigo
                                )
                            )
                            Text(format, color = DocVaultColors.TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportFormatDialog = false }) {
                    Text("Close", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    if (showClearExportsDialog) {
        AlertDialog(
            onDismissRequest = { showClearExportsDialog = false },
            title = { Text("Clear all exports?", color = DocVaultColors.TextPrimary) },
            text = {
                Text(
                    "All files in Downloads/DocVault will be deleted. Your vault documents are not affected.",
                    color = DocVaultColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ExportManager.listExports(context).forEach {
                        ExportManager.deleteExport(context, it.uri)
                    }
                    showClearExportsDialog = false
                }) { Text("Clear", color = DocVaultColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearExportsDialog = false }) {
                    Text("Cancel", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete all documents?", color = DocVaultColors.TextPrimary) },
            text = {
                Text(
                    "This permanently deletes every document in your vault. This cannot be undone.",
                    color = DocVaultColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAllDialog = false
                    onDeleteAllDocuments()
                }) { Text("Delete All", color = DocVaultColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    // ── UI ────────────────────────────────────────────────────────

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DocVaultColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DocVaultColors.TextPrimary
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Security ─────────────────────────────────────────
            SettingsSection(title = "Security") {
                // Biometric lock toggle
                SettingsToggleRow(
                    icon = Icons.Default.Lock,
                    title = "Biometric Lock",
                    subtitle = if (canUseBiometric) "Require fingerprint or face on launch"
                    else "Not available on this device",
                    checked = biometricEnabled && canUseBiometric,
                    enabled = canUseBiometric,
                    onCheckedChange = {
                        biometricEnabled = it
                        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, it).apply()
                    }
                )

                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)

                // Auto-lock timer
                SettingsNavRow(
                    icon = Icons.Default.Timer,
                    title = "Auto-lock Timer",
                    subtitle = when (autoLockMinutes) {
                        0 -> "Immediately"
                        -1 -> "Never"
                        else -> "After $autoLockMinutes minute${if (autoLockMinutes > 1) "s" else ""}"
                    },
                    onClick = { showAutoLockDialog = true }
                )
            }

            // ── Export Defaults ───────────────────────────────────
            SettingsSection(title = "Export Defaults") {
                // Default format
                SettingsNavRow(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Default Export Format",
                    subtitle = defaultExportFormat,
                    onClick = { showExportFormatDialog = true }
                )

                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)

                // PDF include text toggle
                SettingsToggleRow(
                    icon = Icons.Default.PictureAsPdf,
                    title = "PDF: Include Text Page",
                    subtitle = "Append extracted text as final PDF page",
                    checked = pdfIncludeText,
                    enabled = true,
                    onCheckedChange = {
                        pdfIncludeText = it
                        prefs.edit().putBoolean(KEY_PDF_INCLUDE_TEXT, it).apply()
                    }
                )
            }

            // ── Storage ───────────────────────────────────────────
            SettingsSection(title = "Storage") {
                // Vault size
                SettingsInfoRow(
                    icon = Icons.Default.Storage,
                    title = "Vault Size",
                    subtitle = vaultSizeStr
                )

                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)

                // Export size + clear
                SettingsNavRow(
                    icon = Icons.Default.DeleteSweep,
                    title = "Clear All Exports",
                    subtitle = "$exportSizeStr in Downloads/DocVault",
                    onClick = { showClearExportsDialog = true },
                    titleColor = DocVaultColors.Error
                )

                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)

                // Delete all documents
                SettingsNavRow(
                    icon = Icons.Default.Delete,
                    title = "Delete All Documents",
                    subtitle = "Permanently wipe entire vault",
                    onClick = { showDeleteAllDialog = true },
                    titleColor = DocVaultColors.Error
                )
            }

            // ── About ─────────────────────────────────────────────
            SettingsSection(title = "About") {
                SettingsInfoRow(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0 — Phase 2"
                )

                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)

                SettingsInfoRow(
                    icon = Icons.Default.Lock,
                    title = "Privacy",
                    subtitle = "100% offline. No data ever leaves your device."
                )

                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)

                SettingsInfoRow(
                    icon = Icons.Default.Storage,
                    title = "Encryption",
                    subtitle = "SQLCipher AES-256 · Android Keystore"
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Reusable Setting Components ───────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = DocVaultColors.TextTertiary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DocVaultColors.CardSurface)
                .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) DocVaultColors.ElectricIndigo else DocVaultColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) DocVaultColors.TextPrimary else DocVaultColors.TextTertiary
            )
            Text(subtitle, fontSize = 12.sp, color = DocVaultColors.TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DocVaultColors.ElectricIndigo,
                uncheckedThumbColor = DocVaultColors.TextTertiary,
                uncheckedTrackColor = DocVaultColors.DarkBackground
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = DocVaultColors.TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (titleColor == DocVaultColors.Error) DocVaultColors.Error
            else DocVaultColors.ElectricIndigo,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = titleColor)
            Text(subtitle, fontSize = 12.sp, color = DocVaultColors.TextSecondary)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = DocVaultColors.TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = DocVaultColors.ElectricIndigo,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DocVaultColors.TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = DocVaultColors.TextSecondary)
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${bytes / 1024}KB"
    else -> "${"%.1f".format(bytes / (1024f * 1024f))}MB"
}