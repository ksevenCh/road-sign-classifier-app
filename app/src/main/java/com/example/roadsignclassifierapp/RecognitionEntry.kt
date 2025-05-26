package com.example.roadsignclassifierapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recognition_history")
data class RecognitionEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val probability: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val imageData: ByteArray,
)