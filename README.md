# Jigsaw Solver

An Android application that helps solve jigsaw puzzles using computer vision and machine learning techniques.

## Features

- **Photo Capture**: Use your device's camera to capture puzzle images
- **Image Import**: Import puzzle photos from your gallery
- **Piece Detection**: Automatically detect individual jigsaw pieces using OpenCV
- **Edge Classification**: Identify border vs interior pieces
- **Tab/Hole Analysis**: Analyze edge features (tabs, holes, flat edges)
- **Orientation Estimation**: Determine piece rotation
- **Piece Matching**: Suggest matching piece pairs with confidence scores
- **Visual Results**: View analysis results with overlays and comparisons

## Technologies Used

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern Android UI toolkit
- **CameraX**: Camera functionality
- **OpenCV**: Image processing and computer vision
- **MVVM Architecture**: Clean separation of concerns
- **Coroutines**: Asynchronous programming and off-main-thread processing
- **Room Database**: Local data persistence

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture:

- **Model**: Data classes and Room database entities
- **View**: Jetpack Compose UI components
- **ViewModel**: Business logic and state management

## Image Processing Pipeline

1. **Preprocessing**: Convert to grayscale, apply Gaussian blur, adaptive thresholding
2. **Segmentation**: Detect contours to identify individual pieces
3. **Feature Extraction**: Analyze edges, orientation, color, and shape
4. **Matching Algorithm**: Compare pieces using color similarity, edge compatibility, and shape matching

## Building the App

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on an Android device or emulator (requires API 24+)

## Permissions

The app requires the following permissions:
- Camera access for photo capture
- Storage access for importing images

## License

See LICENSE file for details.