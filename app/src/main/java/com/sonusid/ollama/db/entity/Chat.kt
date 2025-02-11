package com.sonusid.ollama.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class Chat(
    @PrimaryKey(autoGenerate = true) val chatId: Int = 0,
    val title: String,
)