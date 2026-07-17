package com.example.documentscanner.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.ui.components.GlassmorphicCard
import com.example.documentscanner.ui.theme.DocVaultColors
import com.example.documentscanner.ui.viewmodel.DocumentListViewModel
import com.example.documentscanner.ui.viewmodel.DocumentListViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

@ExperimentalMaterial3Api
@Composable
fun DocumentListScreen(
    onDocumentClick: (ScannedDocument) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DocumentListViewModel = viewModel(factory = DocumentListViewModelFactory(context))
    val documents by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filtered = documents.filter {
        it.fileName.contains(searchQuery, ignoreCase = true) || it.extractedText.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = DocVaultColors.DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("My Vault (${documents.size})") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DocVaultColors.DarkBackground,
                    titleContentColor = DocVaultColors.TextPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(DocVaultColors.DarkBackground)) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                placeholder = { Text("Search vault...", color = DocVaultColors.TextTertiary) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DocVaultColors.WhiteGlassAlpha,
                    unfocusedContainerColor = DocVaultColors.WhiteGlassAlpha,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = DocVaultColors.TextPrimary,
                    unfocusedTextColor = DocVaultColors.TextPrimary
                )
            )

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DocVaultColors.ElectricIndigo)
                    }
                    documents.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No documents yet", color = DocVaultColors.TextSecondary, textAlign = TextAlign.Center)
                    }
                    filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matches for \"$searchQuery\"", color = DocVaultColors.TextTertiary)
                    }
                    else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered, key = { it.id }) { doc ->
                            DocListCard(doc, onClick = { onDocumentClick(doc) }, onDelete = { viewModel.deleteDocument(doc.id) })
                        }
                    }
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DocVaultColors.ElectricIndigo)
            ) { Text("Back to Home") }
        }
    }
}

@SuppressLint("NonObservableLocale")
@Composable
private fun DocListCard(document: ScannedDocument, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(document.dateCreated))
    val preview = document.extractedText.take(100).ifEmpty { "(No text extracted)" }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(document.fileName, color = DocVaultColors.TextPrimary, fontSize = 14.sp, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(dateStr, color = DocVaultColors.TextTertiary, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Text(preview, color = DocVaultColors.TextSecondary, fontSize = 12.sp, maxLines = 2)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DocVaultColors.Error)
            }
        }
    }
}