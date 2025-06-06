package com.example.roadsignclassifierapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecognitionEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recognitionDao(): RecognitionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recognition_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}