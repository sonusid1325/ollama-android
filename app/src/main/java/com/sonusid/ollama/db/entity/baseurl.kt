package com.sonusid.ollama.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "base_url")
data class BaseUrl(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val isActive: Boolean = false
)
