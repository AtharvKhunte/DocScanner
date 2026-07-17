package com.example.documentscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.theme.DocVaultColors
import androidx.compose.ui.graphics.Color

@ExperimentalMaterial3Api
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

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.ElectricIndigo, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📷  Scan Document", style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = onViewVaultClick,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.EmeraldVerified, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🗂️  My Vault", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}