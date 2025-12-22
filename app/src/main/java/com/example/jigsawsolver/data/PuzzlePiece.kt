package com.example.jigsawsolver.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_pieces")
data class PuzzlePiece(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val puzzleId: String,
    val imageData: ByteArray,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val rotation: Float,
    val edgeType: EdgeType,
    val topEdge: EdgeFeature,
    val rightEdge: EdgeFeature,
    val bottomEdge: EdgeFeature,
    val leftEdge: EdgeFeature,
    val dominantColorR: Int,
    val dominantColorG: Int,
    val dominantColorB: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PuzzlePiece

        if (id != other.id) return false
        if (puzzleId != other.puzzleId) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + puzzleId.hashCode()
        result = 31 * result + imageData.contentHashCode()
        return result
    }
}

enum class EdgeType {
    BORDER,
    INTERIOR
}

enum class EdgeFeature {
    FLAT,    // Border edge
    TAB,     // Outward protrusion
    HOLE     // Inward indentation
}
