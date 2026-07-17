@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.components.GlassmorphicCard
import com.example.documentscanner.ui.theme.DocVaultColors

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onViewVaultClick: () -> Unit
) {
    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("DocVault", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DocVaultColors.DarkBackground,
                    titleContentColor = DocVaultColors.TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DocVaultColors.DarkBackground)
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(DocVaultColors.ElectricIndigo.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔐", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("DocVault", style = MaterialTheme.typography.displayLarge, color = DocVaultColors.TextPrimary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Secure Document Management", style = MaterialTheme.typography.bodyLarge, color = DocVaultColors.TextSecondary, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("100% offline. Encrypted. Private.", style = MaterialTheme.typography.bodyMedium, color = DocVaultColors.TextTertiary, textAlign = TextAlign.Center)
            }

            // Glass Action Cards
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionCard(
                    emoji = "📷",
                    accentColor = DocVaultColors.ElectricIndigo,
                    title = "Scan Document",
                    subtitle = "Capture and digitize",
                    onClick = onScanClick
                )
                HomeActionCard(
                    emoji = "🗂️",
                    accentColor = DocVaultColors.EmeraldVerified,
                    title = "My Vault",
                    subtitle = "View saved documents",
                    onClick = onViewVaultClick
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    emoji: String,
    accentColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = DocVaultColors.TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DocVaultColors.TextTertiary)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DocVaultColors.TextTertiary
            )
        }
    }
}