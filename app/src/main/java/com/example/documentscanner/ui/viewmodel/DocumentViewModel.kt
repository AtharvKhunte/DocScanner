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
import kotlinx.coroutines.launch

sealed class OCRState {
    object Idle : OCRState()
    object Processing : OCRState()
    data class Success(val text: String) : OCRState()
    data class Error(val message: String) : OCRState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val documentId: Long) : SaveState()
    data class Error(val message: String) : SaveState()
}

class DocumentViewModel(
    private val repository: DocumentRepository,
    private val ocrProcessor: OCRProcessor
) : ViewModel() {

    private val _ocrState = MutableStateFlow<OCRState>(OCRState.Idle)
    val ocrState: StateFlow<OCRState> = _ocrState

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText

    /**
     * Extract text from image using ML Kit
     */
    fun extractTextFromImage(imagePath: String) {
        viewModelScope.launch {
            _ocrState.value = OCRState.Processing

            try {
                android.util.Log.d("DocumentViewModel", "Starting OCR for: $imagePath")

                val text = ocrProcessor.extractTextFromImage(imagePath)

                if (text != null && text.isNotEmpty()) {
                    _extractedText.value = text
                    _ocrState.value = OCRState.Success(text)
                    android.util.Log.d("DocumentViewModel", "OCR success: ${text.length} characters")
                } else {
                    _ocrState.value = OCRState.Error("No text found in image")
                    android.util.Log.w("DocumentViewModel", "No text found in image")
                }
            } catch (e: Exception) {
                val errorMsg = "OCR failed: ${e.message}"
                _ocrState.value = OCRState.Error(errorMsg)
                android.util.Log.e("DocumentViewModel", errorMsg, e)
            }
        }
    }

    /**
     * Save document to database with extracted text
     */
    fun saveDocument(
        fileName: String,
        filePath: String,
        extractedText: String
    ) {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            try {
                android.util.Log.d("DocumentViewModel", "Saving document: $fileName")

                val documentId = repository.saveDocumentWithText(
                    fileName = fileName,
                    filePath = filePath,
                    extractedText = extractedText
                )

                _saveState.value = SaveState.Success(documentId)
                android.util.Log.d("DocumentViewModel", "Document saved with ID: $documentId")
            } catch (e: Exception) {
                val errorMsg = "Failed to save: ${e.message}"
                _saveState.value = SaveState.Error(errorMsg)
                android.util.Log.e("DocumentViewModel", errorMsg, e)
            }
        }
    }

    /**
     * Quick save: Extract text and save in one call
     */
    fun extractAndSaveDocument(
        fileName: String,
        filePath: String
    ) {
        viewModelScope.launch {
            _ocrState.value = OCRState.Processing
            _saveState.value = SaveState.Saving

            try {
                // Step 1: Extract text
                android.util.Log.d("DocumentViewModel", "Extract and save: $fileName")

                val text = ocrProcessor.extractTextFromImage(filePath)
                    ?: throw Exception("OCR returned no text")

                _extractedText.value = text
                _ocrState.value = OCRState.Success(text)

                // Step 2: Save to database
                val documentId = repository.saveDocumentWithText(
                    fileName = fileName,
                    filePath = filePath,
                    extractedText = text
                )

                _saveState.value = SaveState.Success(documentId)
                android.util.Log.d("DocumentViewModel", "Extract and save complete: ID $documentId")
            } catch (e: Exception) {
                val errorMsg = "Failed: ${e.message}"
                _ocrState.value = OCRState.Error(errorMsg)
                _saveState.value = SaveState.Error(errorMsg)
                android.util.Log.e("DocumentViewModel", errorMsg, e)
            }
        }
    }

    /**
     * Reset states
     */
    fun resetStates() {
        _ocrState.value = OCRState.Idle
        _saveState.value = SaveState.Idle
        _extractedText.value = ""
    }
}

/**
 * Factory for creating DocumentViewModel with dependencies
 */
class DocumentViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentViewModel::class.java)) {
            val database = DocumentDatabase.getInstance(context)
            val repository = DocumentRepository(database.documentDao())
            val ocrProcessor = OCRProcessor()

            @Suppress("UNCHECKED_CAST")
            return DocumentViewModel(repository, ocrProcessor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/*
ViewModel Explained:

States:
- OCRState: Idle → Processing → Success/Error
- SaveState: Idle → Saving → Success/Error

Methods:
- extractTextFromImage(): Run OCR only
- saveDocument(): Save with existing text
- extractAndSaveDocument(): OCR + Save in one call

Flow Benefits:
- StateFlow: UI observes state changes
- viewModelScope: Cancels coroutines on clear
- Automatic UI updates via collectAsState

Factory Pattern:
- Creates ViewModel with dependencies
- Handles database initialization
- Provides OcrProcessor and Repository
*/