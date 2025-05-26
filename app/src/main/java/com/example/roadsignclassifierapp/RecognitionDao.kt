package com.example.roadsignclassifierapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecognitionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RecognitionEntry)

    @Query("SELECT * FROM recognition_history ORDER BY timestamp DESC")
    suspend fun getAll(): List<RecognitionEntry>

    @Delete
    suspend fun delete(entry: RecognitionEntry)
}