package com.example.jigsawsolver.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzlePieceDao {
    @Query("SELECT * FROM puzzle_pieces WHERE puzzleId = :puzzleId ORDER BY timestamp ASC")
    fun getPiecesByPuzzleId(puzzleId: String): Flow<List<PuzzlePiece>>
    
    @Query("SELECT * FROM puzzle_pieces WHERE id = :id")
    suspend fun getPieceById(id: Int): PuzzlePiece?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPiece(piece: PuzzlePiece): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPieces(pieces: List<PuzzlePiece>)
    
    @Delete
    suspend fun deletePiece(piece: PuzzlePiece)
    
    @Query("DELETE FROM puzzle_pieces WHERE puzzleId = :puzzleId")
    suspend fun deletePuzzle(puzzleId: String)
    
    @Query("SELECT * FROM puzzle_pieces ORDER BY timestamp DESC")
    fun getAllPieces(): Flow<List<PuzzlePiece>>
}
