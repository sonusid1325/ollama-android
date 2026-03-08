package com.sonusid.ollama.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selected_model")
data class SelectedModel(
    @PrimaryKey val baseUrl: String,
    val modelName: String,
)
