package com.example.documentscanner.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.documentscanner.data.database.DocumentDatabase
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentListViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    // Convert Flow to StateFlow using stateIn
    val documents: StateFlow<List<ScannedDocument>> =
        repository.getAllDocuments()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Delete a document from database
     */
    fun deleteDocument(documentId: Int) {
        viewModelScope.launch {
            try {
                Log.d("DocumentListViewModel", "Deleting document: $documentId")
                repository.deleteDocument(documentId)
                Log.d("DocumentListViewModel", "Document deleted successfully")
            } catch (e: Exception) {
                Log.e("DocumentListViewModel", "Error deleting document: ${e.message}", e)
            }
        }
    }
}

/**
 * Factory for creating DocumentListViewModel with dependencies
 */
class DocumentListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentListViewModel::class.java)) {
            val database = DocumentDatabase.getInstance(context)
            val documentDao = database.documentDao()
            val repository = DocumentRepository(documentDao)

            return DocumentListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}