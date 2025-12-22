package com.example.jigsawsolver.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PuzzlePiece::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun puzzlePieceDao(): PuzzlePieceDao
    
    companion object {
        @Volatile
        private var INSTANCE: PuzzleDatabase? = null
        
        fun getDatabase(context: Context): PuzzleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuzzleDatabase::class.java,
                    "puzzle_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
