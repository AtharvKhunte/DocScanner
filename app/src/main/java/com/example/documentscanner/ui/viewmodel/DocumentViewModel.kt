package com.example.documentscanner.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.documentscanner.data.database.DocumentDatabase
import com.example.documentscanner.domain.repository.DocumentRepository
import com.example.documentscanner.utils.OCRProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OCRState {
    object Idle : OCRState()
    object Processing : OCRState()
    object Success : OCRState()
    data class Error(val message: String) : OCRState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val documentId: Int) : SaveState()
    data class Error(val message: String) : SaveState()
}

class DocumentViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val ocrProcessor = OCRProcessor()

    private val _ocrState = MutableStateFlow<OCRState>(OCRState.Idle)
    val ocrState: StateFlow<OCRState> = _ocrState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText.asStateFlow()

    fun extractTextFromImages(imagePaths: List<String>) {
        viewModelScope.launch {
            _ocrState.value = OCRState.Processing
            try {
                val text = ocrProcessor.extractTextFromImages(imagePaths)
                if (text.isNotEmpty()) {
                    _extractedText.value = text
                    _ocrState.value = OCRState.Success
                } else {
                    _ocrState.value = OCRState.Error("No text found")
                }
            } catch (e: Exception) {
                _ocrState.value = OCRState.Error(e.message ?: "OCR failed")
            }
        }
    }

    fun saveDocument(fileName: String, imagePaths: List<String>, extractedText: String) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val id = repository.saveDocumentWithText(
                    fileName,
                    imagePaths.joinToString(","),
                    extractedText
                )
                _saveState.value = SaveState.Success(id.toInt())
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Save failed")
            }
        }
    }

    fun resetStates() {
        _ocrState.value = OCRState.Idle
        _saveState.value = SaveState.Idle
        _extractedText.value = ""
    }
}

class DocumentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            val database = DocumentDatabase.getInstance(context)
            val repository = DocumentRepository(database.documentDao())
            return DocumentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}