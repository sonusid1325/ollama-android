package com.sonusid.ollama.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sonusid.ollama.db.dao.BaseUrlDao
import com.sonusid.ollama.db.entity.BaseUrl

@Database(entities = [BaseUrl::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun baseUrlDao(): BaseUrlDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
