package com.sonusid.ollama.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonusid.ollama.db.dao.ChatDao
import com.sonusid.ollama.db.entity.Chat

@Database(entities = [Chat::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}