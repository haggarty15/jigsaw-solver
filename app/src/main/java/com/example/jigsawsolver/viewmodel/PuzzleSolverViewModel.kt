package com.example.jigsawsolver.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jigsawsolver.data.PieceMatch
import com.example.jigsawsolver.data.PuzzleDatabase
import com.example.jigsawsolver.data.PuzzlePiece
import com.example.jigsawsolver.util.ImageProcessor
import com.example.jigsawsolver.util.PieceMatcherAlgorithm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import java.util.UUID

class PuzzleSolverViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = PuzzleDatabase.getDatabase(application)
    private val dao = database.puzzlePieceDao()
    private val imageProcessor = ImageProcessor()
    private val pieceMatcher = PieceMatcherAlgorithm(imageProcessor)
    
    private val _uiState = MutableStateFlow<PuzzleSolverUiState>(PuzzleSolverUiState.Idle)
    val uiState: StateFlow<PuzzleSolverUiState> = _uiState.asStateFlow()
    
    private val _currentPuzzleId = MutableStateFlow<String?>(null)
    val currentPuzzleId: StateFlow<String?> = _currentPuzzleId.asStateFlow()
    
    init {
        if (!OpenCVLoader.initDebug()) {
            _uiState.value = PuzzleSolverUiState.Error("Failed to load OpenCV")
        }
    }
    
    fun analyzePuzzle(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _uiState.value = PuzzleSolverUiState.Analyzing
                
                val puzzleId = UUID.randomUUID().toString()
                _currentPuzzleId.value = puzzleId
                
                // Process image to detect pieces
                val pieces = imageProcessor.processPuzzleImage(bitmap, puzzleId)
                
                if (pieces.isEmpty()) {
                    _uiState.value = PuzzleSolverUiState.Error("No puzzle pieces found")
                    return@launch
                }
                
                // Save pieces to database
                dao.insertPieces(pieces)
                
                // Find matches
                val matches = pieceMatcher.findMatches(pieces)
                
                _uiState.value = PuzzleSolverUiState.Success(pieces, matches)
            } catch (e: Exception) {
                _uiState.value = PuzzleSolverUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = PuzzleSolverUiState.Idle
        _currentPuzzleId.value = null
    }
}

sealed class PuzzleSolverUiState {
    object Idle : PuzzleSolverUiState()
    object Analyzing : PuzzleSolverUiState()
    data class Success(
        val pieces: List<PuzzlePiece>,
        val matches: List<PieceMatch>
    ) : PuzzleSolverUiState()
    data class Error(val message: String) : PuzzleSolverUiState()
}
