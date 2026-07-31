package com.example.documentscanner.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.theme.DocVaultColors

@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)

    val canUseBiometric = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DocVaultColors.DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // ── Shield Icon ──────────────────────────────────────
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
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security",
                    tint = DocVaultColors.ElectricIndigo,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Title ────────────────────────────────────────────
            Text(
                text = "Secure Your Vault",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DocVaultColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // ── Subtitle ─────────────────────────────────────────
            Text(
                text = if (canUseBiometric)
                    "DocVault will use your biometrics or device PIN to lock access. Your documents stay encrypted on this device only."
                else
                    "Your device doesn't have biometric security. Enable a screen lock in device settings for full protection.",
                fontSize = 14.sp,
                color = DocVaultColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            // ── Feature highlights card ───────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DocVaultColors.CardSurface)
                    .border(1.dp, DocVaultColors.Border, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SetupFeatureRow(
                    icon = Icons.Default.Lock,
                    title = "Biometric Lock",
                    subtitle = "Fingerprint, face, or device PIN"
                )
                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)
                SetupFeatureRow(
                    icon = Icons.Default.Shield,
                    title = "AES-256 Encryption",
                    subtitle = "Protected by Android Keystore"
                )
                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)
                SetupFeatureRow(
                    icon = Icons.Default.Check,
                    title = "100% Offline",
                    subtitle = "No data ever leaves your device"
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── CTA Button ───────────────────────────────────────
            Button(
                onClick = onSetupComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DocVaultColors.ElectricIndigo,
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (canUseBiometric) "Enable Vault Lock" else "Continue",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Footer note ──────────────────────────────────────
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
                    "Your encryption key is generated locally and never shared",
                    fontSize = 11.sp,
                    color = DocVaultColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Feature Row ──────────────────────────────────────────────────

@Composable
private fun SetupFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DocVaultColors.ElectricIndigo.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DocVaultColors.ElectricIndigo,
                modifier = Modifier.size(18.dp)
            )
        }

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DocVaultColors.TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = DocVaultColors.TextSecondary
            )
        }
    }
}