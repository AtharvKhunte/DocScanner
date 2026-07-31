package com.example.documentscanner.ui.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.documentscanner.ui.theme.DocVaultColors

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var authFailed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun showBiometricPrompt() {
        val fragmentActivity = activity ?: return
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            fragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    authFailed = false
                    onUnlocked()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    authFailed = true
                    errorMessage = errString.toString()
                }

                override fun onAuthenticationFailed() {
                    authFailed = true
                    errorMessage = "Authentication failed. Please try again."
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock DocVault")
            .setSubtitle("Use your biometric credential to access the vault")
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Auto-trigger on first composition
    LaunchedEffect(Unit) {
        showBiometricPrompt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DocVaultColors.DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // ── Lock Icon ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DocVaultColors.ElectricIndigo.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        DocVaultColors.ElectricIndigo.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = DocVaultColors.ElectricIndigo,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Title ────────────────────────────────────────────
            Text(
                text = "DocVault Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DocVaultColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // ── Subtitle ─────────────────────────────────────────
            Text(
                text = "Authenticate to access your encrypted vault",
                fontSize = 14.sp,
                color = DocVaultColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Auth status card ─────────────────────────────────
            if (authFailed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DocVaultColors.Error.copy(alpha = 0.08f))
                        .border(1.dp, DocVaultColors.Error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = errorMessage.ifEmpty { "Authentication failed. Please try again." },
                        fontSize = 13.sp,
                        color = DocVaultColors.Error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            // ── Unlock Button ────────────────────────────────────
            Button(
                onClick = {
                    authFailed = false
                    showBiometricPrompt()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DocVaultColors.ElectricIndigo,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Unlock with Biometrics",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Security note ────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = DocVaultColors.TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "AES-256 Encrypted · 100% Offline",
                    fontSize = 11.sp,
                    color = DocVaultColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}