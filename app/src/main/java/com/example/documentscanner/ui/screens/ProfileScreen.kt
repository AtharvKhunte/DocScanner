package com.example.documentscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.utils.AuthRepository
import com.example.documentscanner.utils.SupabaseManager
import com.example.documentscanner.utils.SyncRepository
//import io.github.jan.supabase.plugins.NativeSignInResult
//import io.github.jan.supabase.plugins.composeAuth
//import io.github.jan.supabase.plugins.rememberLoginWithGoogle
import io.github.jan.supabase.compose.auth.ComposeAuth
import kotlinx.coroutines.launch
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberLoginWithGoogle
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
private const val PREFS_SYNC = "docvault_sync_prefs"
private const val KEY_SYNC_ENABLED = "sync_enabled"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoggedIn by remember { mutableStateOf(AuthRepository.isLoggedIn) }
    var userEmail by remember { mutableStateOf(AuthRepository.userEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var syncEnabled by remember {
        mutableStateOf(
            context.getSharedPreferences(PREFS_SYNC, android.content.Context.MODE_PRIVATE)
                .getBoolean(KEY_SYNC_ENABLED, false)
        )
    }
    var syncedCount by remember { mutableStateOf(0) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    // ── Google Sign-In via Compose Auth ──────────────────────────
    val googleLoginAction = SupabaseManager.client.composeAuth.rememberLoginWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    isLoggedIn = true
                    userEmail = AuthRepository.userEmail
                    errorMessage = ""
                    scope.launch {
                        syncedCount = SyncRepository.getSyncedCount()
                        snackbarHostState.showSnackbar("Signed in successfully")
                    }
                }
                is NativeSignInResult.Error -> {
                    errorMessage = result.message
                    isLoading = false
                }
                is NativeSignInResult.NetworkError -> {
                    errorMessage = "Network error: ${result.message}"
                    isLoading = false
                }
                NativeSignInResult.ClosedByUser -> {
                    errorMessage = "Sign in cancelled"
                    isLoading = false
                }
            }
        }
    )

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) syncedCount = SyncRepository.getSyncedCount()
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign out?", color = DocVaultColors.TextPrimary) },
            text = {
                Text(
                    "Your documents stay on this device. Cloud sync pauses until you sign in again.",
                    color = DocVaultColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    scope.launch {
                        AuthRepository.signOut()
                        isLoggedIn = false
                        userEmail = ""
                        syncEnabled = false
                        context.getSharedPreferences(PREFS_SYNC, android.content.Context.MODE_PRIVATE)
                            .edit().putBoolean(KEY_SYNC_ENABLED, false).apply()
                    }
                }) { Text("Sign out", color = DocVaultColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = DocVaultColors.TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DocVaultColors.TextPrimary)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DocVaultColors.DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DocVaultColors.DarkBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLoggedIn) {
                Spacer(Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DocVaultColors.ElectricIndigo.copy(alpha = 0.12f))
                        .border(1.dp, DocVaultColors.ElectricIndigo.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountCircle, null, tint = DocVaultColors.ElectricIndigo, modifier = Modifier.size(44.dp))
                }

                Spacer(Modifier.height(20.dp))

                Text("Sign in to DocVault", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DocVaultColors.TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("Sync documents across all your devices", fontSize = 14.sp, color = DocVaultColors.TextSecondary, textAlign = TextAlign.Center)

                Spacer(Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DocVaultColors.CardSurface)
                        .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileFeatureRow("🔄", "Sync across devices", "Access docs on all your devices")
                    HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)
                    ProfileFeatureRow("🔐", "Passkey login", "Fingerprint, face, or PIN — no password")
                    HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)
                    ProfileFeatureRow("☁️", "Cloud backup", "Never lose your documents")
                }

                Spacer(Modifier.height(32.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, fontSize = 12.sp, color = DocVaultColors.Error, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        googleLoginAction.startFlow()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.ElectricIndigo, contentColor = Color.White)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Uses your Google passkey — no password needed", fontSize = 11.sp, color = DocVaultColors.TextTertiary, textAlign = TextAlign.Center)

            } else {
                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(DocVaultColors.ElectricIndigo.copy(alpha = 0.15f))
                        .border(2.dp, DocVaultColors.ElectricIndigo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DocVaultColors.ElectricIndigo
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(userEmail, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DocVaultColors.TextPrimary)
                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CloudDone, null, tint = DocVaultColors.EmeraldVerified, modifier = Modifier.size(14.dp))
                    Text("Signed in with Google", fontSize = 12.sp, color = DocVaultColors.EmeraldVerified)
                }

                Spacer(Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DocVaultColors.CardSurface)
                        .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (syncEnabled) Icons.Default.CloudSync else Icons.Default.CloudOff,
                                null,
                                tint = if (syncEnabled) DocVaultColors.ElectricIndigo else DocVaultColors.TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text("Cloud Sync", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DocVaultColors.TextPrimary)
                                Text(if (syncEnabled) "Syncing to cloud" else "Sync is off", fontSize = 12.sp, color = DocVaultColors.TextSecondary)
                            }
                        }
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = { enabled ->
                                syncEnabled = enabled
                                context.getSharedPreferences(PREFS_SYNC, android.content.Context.MODE_PRIVATE)
                                    .edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = DocVaultColors.ElectricIndigo,
                                uncheckedThumbColor = DocVaultColors.TextTertiary,
                                uncheckedTrackColor = DocVaultColors.DarkBackground
                            )
                        )
                    }

                    if (syncEnabled) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    syncedCount = SyncRepository.getSyncedCount()
                                    snackbarHostState.showSnackbar("$syncedCount documents synced")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DocVaultColors.ElectricIndigo.copy(alpha = 0.15f),
                                contentColor = DocVaultColors.ElectricIndigo
                            )
                        ) {
                            Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sync Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("$syncedCount documents in cloud", fontSize = 11.sp, color = DocVaultColors.TextTertiary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DocVaultColors.Error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DocVaultColors.Error)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileFeatureRow(emoji: String, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 20.sp)
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DocVaultColors.TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = DocVaultColors.TextSecondary)
        }
    }
}