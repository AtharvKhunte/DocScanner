@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.ui.theme.DocVaultColors

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onViewVaultClick: () -> Unit,
    onViewExportsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
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
                    IconButton(onClick = { onSettingsClick() }) {
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
        },
        bottomBar = {
            BottomAppBar(
                containerColor = DocVaultColors.DarkBackground,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home tab (active)
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Home",
                            tint = DocVaultColors.ElectricIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Vault tab
                    IconButton(onClick = onViewVaultClick) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Vault",
                            tint = DocVaultColors.TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Settings tab
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = DocVaultColors.TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DocVaultColors.DarkBackground)
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Hero Section ─────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Shield Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            DocVaultColors.ElectricIndigo.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "DocVault Shield",
                        tint = DocVaultColors.ElectricIndigo,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Title
                Text(
                    text = "DocVault",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DocVaultColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "100% Offline. Encrypted. Private.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = DocVaultColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // ── Action Buttons ───────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary: Scan Document
                Button(
                    onClick = onScanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DocVaultColors.ElectricIndigo,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Scan Document",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Secondary Row: My Vault + My Exports
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // My Vault
                    Button(
                        onClick = onViewVaultClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DocVaultColors.CardSurface,
                            contentColor = DocVaultColors.TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, DocVaultColors.Border
                        )
                    ) {
                        Text(
                            "My Vault",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // My Exports
                    Button(
                        onClick = onViewExportsClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DocVaultColors.CardSurface,
                            contentColor = DocVaultColors.TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, DocVaultColors.Border
                        )
                    ) {
                        Text(
                            "My Exports",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}