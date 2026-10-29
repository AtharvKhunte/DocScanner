package com.example.documentscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.theme.DocVaultColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DocVaultColors.ElectricIndigo.copy(alpha = 0.12f))
                    .border(1.dp, DocVaultColors.ElectricIndigo.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = DocVaultColors.ElectricIndigo,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Cloud Sync",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DocVaultColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Coming in next update",
                fontSize = 14.sp,
                color = DocVaultColors.TextSecondary,
                textAlign = TextAlign.Center
            )

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
                ComingSoonRow("🔐", "Passkey login", "Fingerprint, face, or PIN — no password")
                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)
                ComingSoonRow("🔄", "Cross-device sync", "Access documents on all your devices")
                HorizontalDivider(color = DocVaultColors.Border, thickness = 0.5.dp)
                ComingSoonRow("☁️", "Cloud backup", "Never lose your documents")
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = DocVaultColors.TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Currently offline-only · sync coming soon",
                    fontSize = 12.sp,
                    color = DocVaultColors.TextTertiary
                )
            }
        }
    }
}

@Composable
private fun ComingSoonRow(emoji: String, title: String, subtitle: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DocVaultColors.TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = DocVaultColors.TextSecondary)
        }
    }
}