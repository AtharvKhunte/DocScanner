@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onViewDocumentsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DocVault") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔐 DocVault", fontSize = 24.sp)
            Text("Your Private Document Vault", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("📸 Scan Document")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewDocumentsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("🔐 My Vault")
            }
        }
    }
}

/*
HomeScreen - Updated for Week 2 Day 5:

Buttons:
1. "Scan New Document" (📷)
   - Opens camera
   - Captures photo
   - Extracts text
   - Saves to database

2. "View My Documents" (📚)
   - Opens DocumentListScreen
   - Shows all saved documents
   - Search functionality
   - Delete documents

Layout:
- Title and description at top
- Both buttons centered
- Clean, simple UI
*/