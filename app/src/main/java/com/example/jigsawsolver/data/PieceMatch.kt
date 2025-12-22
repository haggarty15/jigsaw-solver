package com.example.jigsawsolver.data

data class PieceMatch(
    val piece1: PuzzlePiece,
    val piece2: PuzzlePiece,
    val confidence: Float,
    val matchType: MatchType
)

enum class MatchType {
    COLOR_SIMILARITY,
    EDGE_COMPATIBILITY,
    SHAPE_SIMILARITY,
    COMBINED
}
