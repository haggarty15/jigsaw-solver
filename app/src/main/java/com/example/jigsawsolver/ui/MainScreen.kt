package com.example.jigsawsolver.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.jigsawsolver.viewmodel.PuzzleSolverUiState
import com.example.jigsawsolver.viewmodel.PuzzleSolverViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: PuzzleSolverViewModel,
    onNavigateToCamera: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            selectedBitmap = bitmap
            viewModel.analyzePuzzle(bitmap)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Jigsaw Solver",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        when (uiState) {
            is PuzzleSolverUiState.Idle -> {
                IdleContent(
                    onCapturePhoto = {
                        if (cameraPermissionState.status.isGranted) {
                            onNavigateToCamera()
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    onImportPhoto = {
                        imagePickerLauncher.launch("image/*")
                    }
                )
            }
            is PuzzleSolverUiState.Analyzing -> {
                AnalyzingContent()
            }
            is PuzzleSolverUiState.Success -> {
                val state = uiState as PuzzleSolverUiState.Success
                ResultsContent(
                    pieces = state.pieces,
                    matches = state.matches,
                    onReset = { viewModel.resetState() }
                )
            }
            is PuzzleSolverUiState.Error -> {
                val state = uiState as PuzzleSolverUiState.Error
                ErrorContent(
                    message = state.message,
                    onReset = { viewModel.resetState() }
                )
            }
        }
    }
}

@Composable
fun IdleContent(
    onCapturePhoto: () -> Unit,
    onImportPhoto: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onCapturePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Capture Photo")
        }
        
        Button(
            onClick = onImportPhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Import Photo")
        }
    }
}

@Composable
fun AnalyzingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text("Analyzing puzzle...")
    }
}

@Composable
fun ResultsContent(
    pieces: List<com.example.jigsawsolver.data.PuzzlePiece>,
    matches: List<com.example.jigsawsolver.data.PieceMatch>,
    onReset: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Analysis Results",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total pieces detected: ${pieces.size}")
                    Text("Border pieces: ${pieces.count { it.edgeType == com.example.jigsawsolver.data.EdgeType.BORDER }}")
                    Text("Interior pieces: ${pieces.count { it.edgeType == com.example.jigsawsolver.data.EdgeType.INTERIOR }}")
                    Text("Potential matches found: ${matches.size}")
                }
            }
        }
        
        item {
            Text(
                text = "Top Matches",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(matches.take(10)) { match ->
            MatchCard(match)
        }
        
        item {
            Button(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Analyze Another Puzzle")
            }
        }
    }
}

@Composable
fun MatchCard(match: com.example.jigsawsolver.data.PieceMatch) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Match Confidence: ${String.format("%.1f%%", match.confidence)}",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type: ${match.matchType}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PieceInfo("Piece 1", match.piece1)
                PieceInfo("Piece 2", match.piece2)
            }
        }
    }
}

@Composable
fun PieceInfo(label: String, piece: com.example.jigsawsolver.data.PuzzlePiece) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = "${piece.edgeType}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "RGB(${piece.dominantColorR},${piece.dominantColorG},${piece.dominantColorB})",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ErrorContent(
    message: String,
    onReset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Error: $message",
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onReset) {
            Text("Try Again")
        }
    }
}
