@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.documentscanner.ui.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.ui.viewmodel.DocumentListViewModel
import com.example.documentscanner.ui.viewmodel.DocumentListViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentListScreen(
    onDocumentClick: (ScannedDocument) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DocumentListViewModel = viewModel(
        factory = DocumentListViewModelFactory(context)
    )

    val documents by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredDocuments = documents.filter { doc ->
        doc.fileName.contains(searchQuery, ignoreCase = true) ||
                doc.extractedText.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Documents (${documents.size})") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("🔍 Search documents...") },
                singleLine = true
            )

            Divider()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        LoadingState()
                    }

                    documents.isEmpty() -> {
                        EmptyState(onBack = onBack)
                    }

                    filteredDocuments.isEmpty() -> {
                        NoResultsState(searchQuery = searchQuery)
                    }

                    else -> {
                        DocumentsList(
                            documents = filteredDocuments,
                            onDocumentClick = onDocumentClick,
                            onDeleteClick = { docId ->
                                viewModel.deleteDocument(docId)
                            }
                        )
                    }
                }
            }

            if (documents.isNotEmpty()) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading documents...")
        }
    }
}

@Composable
private fun EmptyState(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📄 No documents yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start by scanning your first document",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
private fun NoResultsState(searchQuery: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "❌ No documents match \"$searchQuery\"",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DocumentsList(
    documents: List<ScannedDocument>,
    onDocumentClick: (ScannedDocument) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            count = documents.size,
            key = { index -> documents[index].id }
        ) { index ->
            val document = documents[index]
            DocumentListItem(
                document = document,
                onItemClick = { onDocumentClick(document) },
                onDeleteClick = { onDeleteClick(document.id) }
            )
            Divider()
        }
    }
}

@Composable
private fun DocumentListItem(
    document: ScannedDocument,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(document.dateCreated))

    val previewText = if (document.extractedText.isNotEmpty()) {
        if (document.extractedText.length > 150) {
            document.extractedText.substring(0, 150) + "..."
        } else {
            document.extractedText
        }
    } else {
        "⏳ No text extracted"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
            Text(
                text = document.fileName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = dateString,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = previewText,
                fontSize = 12.sp,
                color = Color(0xFF555555),
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete document",
                tint = Color.Red
            )
        }
    }
}