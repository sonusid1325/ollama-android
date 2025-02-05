package com.sonusid.ollama.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chat(
    @PrimaryKey val cid: Int,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "content") val content: String?
)