package com.abdurakhmanov.ridez.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abdurakhmanov.ridez.utils.DATABASE_NAME

@Database(entities = [LocationUpdate::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationUpdateDao(): LocationUpdateDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
        }
    }
}