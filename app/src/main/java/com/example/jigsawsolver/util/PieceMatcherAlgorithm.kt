package com.example.jigsawsolver.util

import com.example.jigsawsolver.data.EdgeFeature
import com.example.jigsawsolver.data.MatchType
import com.example.jigsawsolver.data.PieceMatch
import com.example.jigsawsolver.data.PuzzlePiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PieceMatcherAlgorithm(private val imageProcessor: ImageProcessor) {
    
    suspend fun findMatches(pieces: List<PuzzlePiece>, minConfidence: Float = 30f): List<PieceMatch> = withContext(Dispatchers.Default) {
        val matches = mutableListOf<PieceMatch>()
        
        for (i in pieces.indices) {
            for (j in i + 1 until pieces.size) {
                val piece1 = pieces[i]
                val piece2 = pieces[j]
                
                val match = analyzePieceMatch(piece1, piece2)
                if (match.confidence >= minConfidence) {
                    matches.add(match)
                }
            }
        }
        
        matches.sortedByDescending { it.confidence }
    }
    
    private fun analyzePieceMatch(piece1: PuzzlePiece, piece2: PuzzlePiece): PieceMatch {
        val colorScore = calculateColorMatchScore(piece1, piece2)
        val edgeScore = calculateEdgeMatchScore(piece1, piece2)
        val shapeScore = calculateShapeMatchScore(piece1, piece2)
        
        // Weighted combination
        val combinedScore = (colorScore * 0.3f + edgeScore * 0.5f + shapeScore * 0.2f)
        
        return PieceMatch(
            piece1 = piece1,
            piece2 = piece2,
            confidence = combinedScore,
            matchType = MatchType.COMBINED
        )
    }
    
    private fun calculateColorMatchScore(piece1: PuzzlePiece, piece2: PuzzlePiece): Float {
        val color1 = intArrayOf(piece1.dominantColorR, piece1.dominantColorG, piece1.dominantColorB)
        val color2 = intArrayOf(piece2.dominantColorR, piece2.dominantColorG, piece2.dominantColorB)
        
        return imageProcessor.calculateColorSimilarity(color1, color2)
    }
    
    private fun calculateEdgeMatchScore(piece1: PuzzlePiece, piece2: PuzzlePiece): Float {
        var matchCount = 0
        var totalChecks = 0
        
        // Check if any edges are compatible
        val edges1 = listOf(piece1.topEdge, piece1.rightEdge, piece1.bottomEdge, piece1.leftEdge)
        val edges2 = listOf(piece2.topEdge, piece2.rightEdge, piece2.bottomEdge, piece2.leftEdge)
        
        for (edge1 in edges1) {
            for (edge2 in edges2) {
                totalChecks++
                if (imageProcessor.areEdgesCompatible(edge1, edge2)) {
                    matchCount++
                }
            }
        }
        
        return if (totalChecks > 0) (matchCount.toFloat() / totalChecks) * 100 else 0f
    }
    
    private fun calculateShapeMatchScore(piece1: PuzzlePiece, piece2: PuzzlePiece): Float {
        // Compare dimensions and orientations
        val widthDiff = abs(piece1.width - piece2.width) / maxOf(piece1.width, piece2.width)
        val heightDiff = abs(piece1.height - piece2.height) / maxOf(piece1.height, piece2.height)
        val rotationDiff = abs(piece1.rotation - piece2.rotation) / 180f
        
        val dimensionScore = 1f - ((widthDiff + heightDiff) / 2f)
        val rotationScore = 1f - rotationDiff
        
        return ((dimensionScore * 0.6f + rotationScore * 0.4f) * 100).coerceIn(0f, 100f)
    }
}
