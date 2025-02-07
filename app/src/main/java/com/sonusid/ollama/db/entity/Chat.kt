package com.sonusid.ollama.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_table")
data class Chat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val messages: List<String>,
)