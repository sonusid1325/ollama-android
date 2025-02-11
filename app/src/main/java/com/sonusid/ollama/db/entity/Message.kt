package com.sonusid.ollama.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters // Add this import

@Entity(tableName = "chat_table")
data class Message(
    @PrimaryKey(autoGenerate = true) val messageID: Int = 0,
    val message: String,
)