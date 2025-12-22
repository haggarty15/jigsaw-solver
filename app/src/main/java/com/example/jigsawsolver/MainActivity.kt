package com.example.jigsawsolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jigsawsolver.ui.CameraScreen
import com.example.jigsawsolver.ui.MainScreen
import com.example.jigsawsolver.ui.theme.JigsawSolverTheme
import com.example.jigsawsolver.viewmodel.PuzzleSolverViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JigsawSolverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JigsawSolverApp()
                }
            }
        }
    }
}

@Composable
fun JigsawSolverApp() {
    val viewModel: PuzzleSolverViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    
    when (currentScreen) {
        Screen.Main -> {
            MainScreen(
                viewModel = viewModel,
                onNavigateToCamera = { currentScreen = Screen.Camera }
            )
        }
        Screen.Camera -> {
            CameraScreen(
                viewModel = viewModel,
                onBack = { currentScreen = Screen.Main },
                onPhotoCaptured = { currentScreen = Screen.Main }
            )
        }
    }
}

sealed class Screen {
    object Main : Screen()
    object Camera : Screen()
}
