package com.sonusid.ollama.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_table")
data class Message(
    @PrimaryKey(autoGenerate = true) val messageID: Int = 0,
    val chatId: Int,
    val message: String,
    val isSendbyMe: Boolean
)