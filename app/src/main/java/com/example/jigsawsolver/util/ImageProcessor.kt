package com.example.jigsawsolver.util

import android.graphics.Bitmap
import com.example.jigsawsolver.data.EdgeFeature
import com.example.jigsawsolver.data.EdgeType
import com.example.jigsawsolver.data.PuzzlePiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.sqrt

class ImageProcessor {
    
    suspend fun processPuzzleImage(bitmap: Bitmap, puzzleId: String = UUID.randomUUID().toString()): List<PuzzlePiece> = withContext(Dispatchers.Default) {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        
        // Preprocessing
        val preprocessed = preprocessImage(mat)
        
        // Detect contours
        val contours = detectContours(preprocessed)
        
        // Filter and analyze pieces
        val pieces = mutableListOf<PuzzlePiece>()
        for ((index, contour) in contours.withIndex()) {
            val piece = analyzePiece(contour, mat, puzzleId, index)
            piece?.let { pieces.add(it) }
        }
        
        mat.release()
        preprocessed.release()
        
        pieces
    }
    
    private fun preprocessImage(src: Mat): Mat {
        val gray = Mat()
        val blurred = Mat()
        val thresh = Mat()
        
        // Convert to grayscale
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        
        // Apply Gaussian blur
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
        
        // Apply adaptive thresholding
        Imgproc.adaptiveThreshold(
            blurred,
            thresh,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            11,
            2.0
        )
        
        gray.release()
        blurred.release()
        
        return thresh
    }
    
    private fun detectContours(preprocessed: Mat): List<MatOfPoint> {
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        
        Imgproc.findContours(
            preprocessed,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        )
        
        hierarchy.release()
        
        // Filter contours by area
        val minArea = 500.0
        return contours.filter { Imgproc.contourArea(it) > minArea }
    }
    
    private fun analyzePiece(
        contour: MatOfPoint,
        original: Mat,
        puzzleId: String,
        index: Int
    ): PuzzlePiece? {
        val area = Imgproc.contourArea(contour)
        if (area < 500) return null
        
        // Get bounding rectangle
        val rect = Imgproc.boundingRect(contour)
        
        // Extract piece ROI
        val roi = original.submat(rect)
        val pieceBitmap = Bitmap.createBitmap(roi.cols(), roi.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(roi, pieceBitmap)
        
        // Convert bitmap to byte array
        val stream = ByteArrayOutputStream()
        pieceBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageData = stream.toByteArray()
        
        // Calculate moments for center and orientation
        val moments = Imgproc.moments(contour)
        val centerX = (moments.m10 / moments.m00).toFloat()
        val centerY = (moments.m01 / moments.m00).toFloat()
        
        // Estimate orientation
        val rotation = estimateOrientation(moments)
        
        // Classify edge type
        val edgeType = classifyEdgeType(contour, original.size())
        
        // Analyze edges
        val edges = analyzeEdges(contour, rect)
        
        // Extract dominant color
        val dominantColor = extractDominantColor(roi)
        
        roi.release()
        
        return PuzzlePiece(
            puzzleId = puzzleId,
            imageData = imageData,
            centerX = centerX,
            centerY = centerY,
            width = rect.width.toFloat(),
            height = rect.height.toFloat(),
            rotation = rotation,
            edgeType = edgeType,
            topEdge = edges[0],
            rightEdge = edges[1],
            bottomEdge = edges[2],
            leftEdge = edges[3],
            dominantColorR = dominantColor[0],
            dominantColorG = dominantColor[1],
            dominantColorB = dominantColor[2]
        )
    }
    
    private fun estimateOrientation(moments: Imgproc.Moments): Float {
        val angle = 0.5 * atan2(2 * moments.mu11, moments.mu20 - moments.mu02)
        return Math.toDegrees(angle).toFloat()
    }
    
    private fun classifyEdgeType(contour: MatOfPoint, imageSize: Size): EdgeType {
        val rect = Imgproc.boundingRect(contour)
        val threshold = 10.0
        
        // Check if piece is near image border
        val nearLeft = rect.x < threshold
        val nearTop = rect.y < threshold
        val nearRight = (rect.x + rect.width) > (imageSize.width - threshold)
        val nearBottom = (rect.y + rect.height) > (imageSize.height - threshold)
        
        return if (nearLeft || nearTop || nearRight || nearBottom) {
            EdgeType.BORDER
        } else {
            EdgeType.INTERIOR
        }
    }
    
    private fun analyzeEdges(contour: MatOfPoint, rect: Rect): List<EdgeFeature> {
        // Simplified edge analysis
        // In a real implementation, this would analyze convexity defects
        val points = contour.toArray()
        
        // Divide contour into 4 regions (top, right, bottom, left)
        val edges = mutableListOf<EdgeFeature>()
        
        for (side in 0..3) {
            val feature = analyzeEdgeSide(points, rect, side)
            edges.add(feature)
        }
        
        return edges
    }
    
    private fun analyzeEdgeSide(points: Array<Point>, rect: Rect, side: Int): EdgeFeature {
        // Filter points for this side
        val sidePoints = when (side) {
            0 -> points.filter { it.y < rect.y + rect.height * 0.25 } // Top
            1 -> points.filter { it.x > rect.x + rect.width * 0.75 }  // Right
            2 -> points.filter { it.y > rect.y + rect.height * 0.75 } // Bottom
            else -> points.filter { it.x < rect.x + rect.width * 0.25 } // Left
        }
        
        if (sidePoints.isEmpty()) return EdgeFeature.FLAT
        
        // Calculate deviation from straight line
        val avgY = sidePoints.map { it.y }.average()
        val avgX = sidePoints.map { it.x }.average()
        
        val deviation = when (side) {
            0, 2 -> sidePoints.map { (it.y - avgY) * (it.y - avgY) }.average()
            else -> sidePoints.map { (it.x - avgX) * (it.x - avgX) }.average()
        }
        
        return when {
            deviation < 10 -> EdgeFeature.FLAT
            sidePoints.any { 
                when (side) {
                    0 -> it.y < avgY - 5
                    1 -> it.x > avgX + 5
                    2 -> it.y > avgY + 5
                    else -> it.x < avgX - 5
                }
            } -> EdgeFeature.TAB
            else -> EdgeFeature.HOLE
        }
    }
    
    private fun extractDominantColor(roi: Mat): IntArray {
        val mean = Core.mean(roi)
        return intArrayOf(
            mean.`val`[0].toInt().coerceIn(0, 255),
            mean.`val`[1].toInt().coerceIn(0, 255),
            mean.`val`[2].toInt().coerceIn(0, 255)
        )
    }
    
    fun calculateColorSimilarity(color1: IntArray, color2: IntArray): Float {
        val dr = color1[0] - color2[0]
        val dg = color1[1] - color2[1]
        val db = color1[2] - color2[2]
        val distance = sqrt((dr * dr + dg * dg + db * db).toDouble())
        val maxDistance = sqrt(3.0 * 255 * 255)
        return (1.0 - distance / maxDistance).toFloat() * 100
    }
    
    fun areEdgesCompatible(edge1: EdgeFeature, edge2: EdgeFeature): Boolean {
        return (edge1 == EdgeFeature.TAB && edge2 == EdgeFeature.HOLE) ||
               (edge1 == EdgeFeature.HOLE && edge2 == EdgeFeature.TAB) ||
               (edge1 == EdgeFeature.FLAT && edge2 == EdgeFeature.FLAT)
    }
}
