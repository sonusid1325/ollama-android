package com.sonusid.ollama.db.entity

import androidx.room.TypeConverter
import com.google.gson.Gson

class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        val gson = Gson()
        return gson.fromJson(value, Array<String>::class.java).toList()
    }

    @TypeConverter
    fun toString(value: List<String>): String {
        val gson = Gson()
        return gson.toJson(value)
    }
}