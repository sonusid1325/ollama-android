package com.sonusid.ollama.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sonusid.ollama.db.dao.BaseUrlDao
import com.sonusid.ollama.db.dao.ChatDao
import com.sonusid.ollama.db.dao.MessageDao
import com.sonusid.ollama.db.entity.Chat
import com.sonusid.ollama.db.entity.Message

@Database(entities = [Chat::class, Message::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat-database"
                ).build()
                INSTANCE = db
                db
            }
        }
    }
}