# Jigsaw Solver

An Android application that helps solve jigsaw puzzles using computer vision and machine learning techniques, with cloud-powered solving capabilities and visual solution mapping.

## Features

- **Photo Capture**: Use your device's camera to capture puzzle images
- **Image Import**: Import puzzle photos from your gallery
- **Piece Detection**: Automatically detect individual jigsaw pieces using OpenCV
- **Edge Classification**: Identify border vs interior pieces
- **Tab/Hole Analysis**: Analyze edge features (tabs, holes, flat edges)
- **Orientation Estimation**: Determine piece rotation
- **Piece Matching**: Suggest matching piece pairs with confidence scores
- **Cloud-Powered Solving**: Leverage GCP for compute-intensive puzzle solving
- **Solution Visualization**: Generate numbered solution maps showing piece placement
- **Visual Results**: View analysis results with overlays and comparisons

## Technologies Used

### Mobile (Android)
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern Android UI toolkit
- **CameraX**: Camera functionality
- **OpenCV**: Image processing and computer vision
- **MVVM Architecture**: Clean separation of concerns
- **Coroutines**: Asynchronous programming and off-main-thread processing
- **Room Database**: Local data persistence
- **Retrofit**: API communication with backend services

### Cloud Infrastructure (GCP)
- **Cloud Run**: Serverless container hosting for solving API
- **Compute Engine**: GPU-accelerated instances for complex puzzles
- **Vertex AI**: Machine learning model hosting for piece classification
- **Cloud Storage**: Puzzle image and solution storage
- **Cloud Functions**: Event-driven processing and image generation
- **Imagen API**: AI-powered solution map generation
- **Cloud Build**: CI/CD pipeline

## Architecture

### Mobile Architecture (MVVM)
- **Model**: Data classes and Room database entities
- **View**: Jetpack Compose UI components
- **ViewModel**: Business logic and state management

### Cloud Architecture (Microservices)
- **API Gateway**: Cloud Run endpoint for mobile requests
- **Solver Service**: Distributed solving algorithm with graph optimization
- **Image Processing Service**: OpenCV-based piece analysis at scale
- **Visualization Service**: Solution map generation with piece numbering
- **ML Service**: Vertex AI models for piece shape/pattern recognition

## Image Processing Pipeline

1. **Preprocessing**: Convert to grayscale, apply Gaussian blur, adaptive thresholding
2. **Segmentation**: Detect contours to identify individual pieces
3. **Feature Extraction**: Analyze edges, orientation, color, and shape
4. **Matching Algorithm**: Compare pieces using color similarity, edge compatibility, and shape matching
5. **Cloud Solving**: Send to GCP for optimized graph-based solving
6. **Solution Generation**: Create numbered visualization map

## Building the App

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on an Android device or emulator (requires API 24+)

## Permissions

The app requires the following permissions:

- Camera access for photo capture
- Storage access for importing images
- Internet access for cloud services

---

## 🚀 GCP Integration & Solution Visualization - Implementation Plan

### **Phase 1: GCP Infrastructure Setup** (Week 1-2)

#### 1.1 Project & Service Configuration
- [ ] Create GCP project: `jigsaw-solver-prod`
- [ ] Enable required APIs:
  - Cloud Run API
  - Compute Engine API
  - Vertex AI API
  - Cloud Storage API
  - Cloud Functions API
  - Imagen API (Vertex AI Vision)
  - Cloud Build API
- [ ] Set up billing alerts and quotas
- [ ] Configure IAM roles and service accounts
- [ ] Set up VPC network for secure communication

#### 1.2 Cloud Storage Architecture
- [ ] Create buckets:
  - `jigsaw-puzzle-uploads` (puzzle images from mobile)
  - `jigsaw-piece-data` (extracted piece data)
  - `jigsaw-solutions` (solved puzzle results)
  - `jigsaw-visualization-maps` (generated solution images)
- [ ] Configure lifecycle policies (auto-delete after 30 days)
- [ ] Set up signed URLs for secure uploads
- [ ] Enable versioning for solution tracking

#### 1.3 Monitoring & Logging
- [ ] Set up Cloud Monitoring dashboards
- [ ] Configure Cloud Logging for all services
- [ ] Create alerting policies for:
  - High API latency (>5s)
  - Error rates (>5%)
  - Storage usage (>80% quota)
- [ ] Enable Cloud Trace for distributed tracing

---

### **Phase 2: Backend Solver Service** (Week 3-5)

#### 2.1 Solver API (Cloud Run)
**Technology**: Python/FastAPI or Node.js/Express containerized on Cloud Run

**Responsibilities**:
- Receive puzzle piece data from mobile app
- Orchestrate solving process
- Return solution graph with piece positions

**Endpoints**:
```
POST /api/v1/puzzle/upload
POST /api/v1/puzzle/solve
GET  /api/v1/puzzle/{id}/status
GET  /api/v1/puzzle/{id}/solution
POST /api/v1/puzzle/{id}/visualize
```

**Implementation Tasks**:
- [ ] Create FastAPI application with async support
- [ ] Implement request validation with Pydantic models
- [ ] Build piece data ingestion endpoint
- [ ] Create authentication middleware (API keys + JWT)
- [ ] Add request rate limiting
- [ ] Containerize with Docker (multi-stage build)
- [ ] Deploy to Cloud Run with:
  - Auto-scaling (0-100 instances)
  - Min instances: 1 (avoid cold starts)
  - Memory: 2GB
  - CPU: 2 vCPUs
  - Timeout: 300s

#### 2.2 Advanced Solving Algorithm
**Why GCP**: Complex graph optimization benefits from dedicated compute resources

**Approach**: Graph-based constraint satisfaction with backtracking

**Tasks**:
- [ ] Design puzzle graph data structure:
  ```python
  class PuzzleGraph:
      nodes: List[PieceNode]  # Each piece
      edges: List[EdgeConstraint]  # Compatibility constraints
      solution_grid: Grid2D  # Final placement
  ```
- [ ] Implement edge compatibility matrix calculation
- [ ] Build constraint propagation algorithm:
  - Border piece placement first
  - Corner detection and placement
  - Progressive filling using color gradients
- [ ] Add backtracking with heuristics:
  - Most constrained piece first
  - Look-ahead pruning
- [ ] Optimize with NetworkX graph library
- [ ] Add parallel processing for large puzzles (>500 pieces)

#### 2.3 GPU-Accelerated Solving (Compute Engine)
**For Large Puzzles**: >1000 pieces require GPU acceleration

**Setup**:
- [ ] Create Compute Engine instance template:
  - Machine type: `n1-standard-8`
  - GPU: NVIDIA T4 (1 GPU)
  - Image: Deep Learning VM (TensorFlow/PyTorch)
  - Preemptible: Yes (cost savings)
- [ ] Implement CUDA-based piece matching:
  ```python
  # Parallel edge comparison on GPU
  # Process 1M+ comparisons simultaneously
  ```
- [ ] Create job queue system:
  - Cloud Tasks for job scheduling
  - Auto-spin up GPU instance when needed
  - Auto-shutdown after 5 min idle
- [ ] Build cost-optimized routing:
  - <500 pieces → Cloud Run (CPU)
  - 500-1000 pieces → Cloud Run (high CPU)
  - >1000 pieces → Compute Engine (GPU)

---

### **Phase 3: ML-Enhanced Piece Recognition** (Week 6-7)

#### 3.1 Vertex AI Model Training
**Purpose**: Improve piece shape classification and pattern matching

**Model Types**:
1. **Edge Shape Classifier** (CNN)
   - Input: Piece edge contour image (64x64)
   - Output: [TAB, HOLE, FLAT, IRREGULAR] + confidence
   - Training data: 10K+ labeled piece edges

2. **Pattern Similarity Model** (Siamese Network)
   - Input: Two piece images
   - Output: Similarity score (0-1)
   - Use case: Color/pattern continuity matching

**Tasks**:
- [ ] Prepare training dataset:
  - Collect diverse jigsaw puzzle images
  - Extract and label 10K+ piece edges
  - Augment data (rotation, scaling, noise)
- [ ] Train edge classifier on Vertex AI:
  - Framework: TensorFlow/Keras
  - Training job: Custom container on Vertex AI
  - Hyperparameter tuning with Vertex AI Vizier
- [ ] Train pattern similarity model:
  - Architecture: Siamese CNN with contrastive loss
  - Transfer learning from ResNet50
- [ ] Deploy models to Vertex AI Prediction endpoints
- [ ] Create model versioning strategy
- [ ] Set up A/B testing framework

#### 3.2 Model Integration
- [ ] Update solver service to call Vertex AI endpoints
- [ ] Implement fallback to rule-based classification
- [ ] Add model prediction caching (Redis on Cloud Memorystore)
- [ ] Monitor model accuracy and drift
- [ ] Create retraining pipeline (monthly)

---

### **Phase 4: Solution Visualization Service** (Week 8-9)

#### 4.1 Solution Map Generator (Cloud Functions)
**Purpose**: Generate visual guide showing numbered pieces and their target positions

**Output Format**: 
```
┌─────────────────────────────────┐
│  Solution Map for 500-piece     │
│  Christmas Puzzle               │
├─────────────────────────────────┤
│                                 │
│  [Layout Image]                 │
│   ┌───┬───┬───┐                │
│   │ 1 │ 2 │ 3 │  ← Numbers     │
│   ├───┼───┼───┤    on pieces   │
│   │ 4 │ 5 │ 6 │                │
│   └───┴───┴───┘                │
│                                 │
│  [Piece Reference Grid]         │
│   1: Corner (top-left)          │
│   2: Edge (top)                 │
│   3: Corner (top-right)         │
│   ...                           │
└─────────────────────────────────┘
```

**Implementation**:
- [ ] Create Cloud Function (Gen 2):
  - Trigger: HTTP request from solver service
  - Runtime: Python 3.11
  - Memory: 4GB
  - Timeout: 540s (max)
  
- [ ] **Option A: Programmatic Generation** (PIL/Pillow)
  ```python
  def generate_solution_map(solution_data):
      # Create base canvas
      canvas = Image.new('RGB', (3000, 4000), 'white')
      draw = ImageDraw.Draw(canvas)
      
      # Draw grid layout
      for piece in solution_data.pieces:
          x, y = piece.target_position
          draw_piece_outline(piece, x, y)
          draw_number(piece.number, x, y)
      
      # Add legend/reference
      draw_piece_reference_table(solution_data)
      
      # Add visual enhancements
      add_color_coded_borders()
      add_grid_lines()
      
      return canvas
  ```
  
  **Tasks**:
  - [ ] Implement grid layout calculator
  - [ ] Create piece outline rendering:
    - Use actual piece contours from OpenCV
    - Scale to fit grid cells
  - [ ] Add number overlay:
    - Font: Bold, high contrast
    - Position: Center of piece
    - Background circle for readability
  - [ ] Generate piece reference table:
    - Thumbnail of actual piece image
    - Number
    - Position (row, col)
    - Edge types
  - [ ] Add visual guides:
    - Color-code corners (red)
    - Color-code edges (blue)
    - Color-code interior (gray)
  - [ ] Export as high-res PNG (300 DPI)

- [ ] **Option B: AI-Powered Generation** (Vertex AI Imagen)
  ```python
  def generate_ai_solution_map(solution_data):
      # Create structured prompt
      prompt = f"""
      Create a jigsaw puzzle solution map:
      - {solution_data.total_pieces} pieces
      - Grid: {solution_data.rows}x{solution_data.cols}
      - Each piece numbered 1-{solution_data.total_pieces}
      - Clear, legible numbers on each piece
      - Professional layout with legend
      """
      
      # Call Imagen API
      response = imagen_client.generate_images(
          prompt=prompt,
          number_of_images=1,
          aspect_ratio="4:5",
          safety_filter_level="block_few"
      )
      
      # Overlay actual piece positions
      annotate_with_solution_data(response.image)
      
      return enhanced_image
  ```
  
  **Tasks**:
  - [ ] Set up Vertex AI Imagen client
  - [ ] Design prompt engineering strategy
  - [ ] Implement hybrid approach:
    - Imagen for base aesthetic layout
    - Programmatic overlay for accurate numbers
  - [ ] Add post-processing:
    - Overlay actual piece thumbnails
    - Add interactive elements (for web view)

#### 4.2 Dual Map Generation Strategy
**Generate TWO complementary maps**:

1. **Positioning Map** (shows WHERE each numbered piece goes)
   - Grid with numbers in target positions
   - Color-coded by region
   - Print-friendly

2. **Reference Map** (shows WHAT each numbered piece looks like)
   - Array of piece thumbnails with numbers
   - Sorted by number
   - Easy lookup

**Tasks**:
- [ ] Implement dual map generation
- [ ] Create combined PDF output:
  - Page 1: Positioning map (A3 size)
  - Page 2: Reference map (A4 size)
  - Metadata: puzzle info, difficulty, piece count
- [ ] Add QR code linking to digital version
- [ ] Store in Cloud Storage with public URL

#### 4.3 Mobile Integration
- [ ] Add "Generate Solution Map" button in Android app
- [ ] Create API call to visualization service:
  ```kotlin
  suspend fun generateSolutionMap(puzzleId: String): SolutionMap {
      val response = apiService.generateVisualization(puzzleId)
      return SolutionMap(
          positioningMapUrl = response.positioning_map_url,
          referenceMapUrl = response.reference_map_url,
          pdfUrl = response.pdf_url
      )
  }
  ```
- [ ] Implement image viewing in Compose:
  - Pinch-to-zoom
  - Pan navigation
  - Download to gallery option
- [ ] Add sharing functionality:
  - Share PDF via email/messaging
  - Print directly from app
  - Save to Google Drive

---

### **Phase 5: Mobile App Backend Integration** (Week 10-11)

#### 5.1 API Client Implementation
- [ ] Add Retrofit dependencies to `build.gradle.kts`
- [ ] Create API service interface:
  ```kotlin
  interface JigsawSolverApi {
      @POST("api/v1/puzzle/upload")
      suspend fun uploadPuzzle(
          @Body request: PuzzleUploadRequest
      ): PuzzleUploadResponse
      
      @POST("api/v1/puzzle/solve")
      suspend fun solvePuzzle(
          @Body request: SolveRequest
      ): SolveResponse
      
      @GET("api/v1/puzzle/{id}/solution")
      suspend fun getSolution(
          @Path("id") puzzleId: String
      ): SolutionResponse
      
      @POST("api/v1/puzzle/{id}/visualize")
      suspend fun generateVisualization(
          @Path("id") puzzleId: String
      ): VisualizationResponse
  }
  ```

- [ ] Implement repository pattern:
  ```kotlin
  class PuzzleSolverRepository(
      private val api: JigsawSolverApi,
      private val localDao: PuzzlePieceDao
  ) {
      suspend fun solvePuzzleInCloud(
          pieces: List<PuzzlePiece>
      ): Result<PuzzleSolution>
  }
  ```

- [ ] Add authentication interceptor:
  - API key in headers
  - Token refresh logic
  - Retry on 401

#### 5.2 ViewModel Updates
- [ ] Modify `PuzzleSolverViewModel`:
  ```kotlin
  fun solvePuzzleWithCloud(bitmap: Bitmap) {
      viewModelScope.launch {
          _uiState.value = PuzzleSolverUiState.Analyzing
          
          // Local processing
          val pieces = imageProcessor.processPuzzleImage(bitmap, puzzleId)
          
          // Upload to cloud
          _uiState.value = PuzzleSolverUiState.UploadingToCloud
          repository.uploadPuzzle(pieces)
          
          // Cloud solving
          _uiState.value = PuzzleSolverUiState.SolvingInCloud
          val solution = repository.solvePuzzleInCloud(pieces)
          
          // Generate visualization
          _uiState.value = PuzzleSolverUiState.GeneratingMap
          val visualMap = repository.generateSolutionMap(puzzleId)
          
          _uiState.value = PuzzleSolverUiState.Success(
              pieces, solution.matches, visualMap
          )
      }
  }
  ```

- [ ] Add new UI states:
  - UploadingToCloud
  - SolvingInCloud
  - GeneratingMap

#### 5.3 UI Updates
- [ ] Create new Composables:
  - `SolutionMapViewer`: Display generated maps
  - `CloudSolvingProgress`: Show cloud processing status
  - `DownloadButton`: Download maps to device

- [ ] Update `MainScreen`:
  - Add "Solve with Cloud" toggle
  - Show cost estimate (optional)
  - Display processing time estimate

- [ ] Add settings screen:
  - Cloud solving preferences
  - Quality settings (speed vs. accuracy)
  - Auto-generate map toggle

---

### **Phase 6: Advanced Features & Optimization** (Week 12-14)

#### 6.1 Intelligent Piece Grouping
**GCP Use Case**: Cloud Functions for pre-solving analysis

- [ ] Create piece clustering service:
  - Group by dominant color
  - Group by edge patterns
  - Identify distinct regions (sky, grass, building, etc.)
- [ ] Implement on Cloud Functions:
  - Triggered after piece upload
  - Uses Vertex AI Vision for scene understanding
  - Returns piece groups to improve solving speed

#### 6.2 Progressive Solving
**Real-time Updates**: Stream solving progress to mobile

- [ ] Implement Server-Sent Events (SSE) or WebSocket
- [ ] Show piece placement in real-time:
  ```kotlin
  // Mobile receives updates as pieces are placed
  solutionFlow.collect { update ->
      when (update) {
          is PiecePlaced -> showPieceAnimation(update)
          is RegionComplete -> highlightRegion(update)
          is SolutionComplete -> showCelebration()
      }
  }
  ```

- [ ] Add pause/resume functionality
- [ ] Allow manual piece placement hints:
  - User can mark "definitely borders"
  - Override ML classifications

#### 6.3 Augmented Reality Preview
**Bonus Feature**: AR view of solution

- [ ] Integrate ARCore
- [ ] Overlay solution map on real puzzle table
- [ ] Highlight next piece to place
- [ ] Show rotation guidance

#### 6.4 Cost Optimization
- [ ] Implement caching strategy:
  - Cache piece analysis (Redis)
  - Cache similar puzzle solutions
  - CDN for visualization maps (Cloud CDN)
  
- [ ] Add pricing tiers:
  - Free: Local solving only
  - Basic: Cloud solving <100 pieces
  - Premium: Unlimited + GPU acceleration + AR

- [ ] Monitor and optimize:
  - Set budget alerts
  - Use preemptible instances where possible
  - Implement request batching

---

### **Phase 7: Testing & Deployment** (Week 15-16)

#### 7.1 Backend Testing
- [ ] Unit tests for solving algorithms (pytest)
- [ ] Integration tests for API endpoints
- [ ] Load testing with k6/Locust:
  - Simulate 1000 concurrent users
  - Test with various puzzle sizes
- [ ] GPU performance benchmarking

#### 7.2 Mobile Testing
- [ ] Unit tests for ViewModel and Repository
- [ ] UI tests with Compose testing
- [ ] Integration tests with mock backend
- [ ] End-to-end tests with real GCP staging environment

#### 7.3 CI/CD Pipeline
- [ ] Set up Cloud Build triggers:
  - Backend: Build and deploy on push to `main`
  - Mobile: Build APK on PR
  
- [ ] Configure deployment environments:
  - Development
  - Staging
  - Production
  
- [ ] Implement blue-green deployment for Cloud Run

#### 7.4 Production Launch
- [ ] Deploy backend services
- [ ] Release mobile app to internal testing (Google Play Console)
- [ ] Monitor metrics for 1 week
- [ ] Gradual rollout: 10% → 50% → 100%
- [ ] Prepare rollback plan

---

### **Phase 8: Monitoring & Iteration** (Ongoing)

#### 8.1 Analytics
- [ ] Track key metrics:
  - Solving success rate
  - Average solving time by piece count
  - Visualization generation time
  - User engagement with solution maps
  - Cost per puzzle solved

#### 8.2 User Feedback Loop
- [ ] Add in-app feedback mechanism
- [ ] Collect difficult puzzle examples for ML retraining
- [ ] A/B test visualization styles

#### 8.3 Continuous Improvement
- [ ] Monthly model retraining with new data
- [ ] Algorithm optimization based on metrics
- [ ] Cost reduction initiatives
- [ ] Feature expansion based on user requests

---

## 📊 GCP Services Summary

| Service | Purpose | Estimated Cost (Monthly)* |
|---------|---------|--------------------------|
| **Cloud Run** | Solver API hosting | $20-50 (pay-per-request) |
| **Compute Engine** | GPU solving (>1000 pieces) | $50-200 (preemptible) |
| **Vertex AI** | ML models (edge classifier) | $30-80 (prediction costs) |
| **Cloud Storage** | Image/solution storage | $5-15 (5GB storage) |
| **Cloud Functions** | Visualization generation | $10-30 (invocations) |
| **Imagen API** | AI map generation | $20-60 (pay-per-image) |
| **Cloud Build** | CI/CD pipelines | Free tier sufficient |
| **Cloud CDN** | Map delivery | $5-20 (bandwidth) |
| **Cloud Memorystore** | Redis caching | $25 (basic instance) |
| **Cloud Monitoring** | Logging and metrics | Free tier sufficient |
| **TOTAL** | | **~$165-475/month** |

*Based on moderate usage (100 puzzles solved/day)

---

## 🎯 Success Metrics

- **Solving Accuracy**: >90% correct piece placement
- **Solving Speed**: <30 seconds for 100-piece puzzle, <5 min for 500-piece
- **Visualization Quality**: >4.5/5 user rating
- **Cloud Uptime**: 99.9% SLA
- **Cost Efficiency**: <$0.50 per puzzle solved
- **User Satisfaction**: >85% would recommend

---

## Building the App

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Configure GCP credentials (add `google-services.json`)
5. Run on an Android device or emulator (requires API 24+)

## Permissions

The app requires the following permissions:

- Camera access for photo capture
- Storage access for importing images
- Internet access for cloud services

## License

See LICENSE file for details.
