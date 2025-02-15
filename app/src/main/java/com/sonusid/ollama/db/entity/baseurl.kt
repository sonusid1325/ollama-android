package com.sonusid.ollama.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "base_url")
data class BaseUrl(
    @PrimaryKey val id: Int = 1,
    val url: String
)
