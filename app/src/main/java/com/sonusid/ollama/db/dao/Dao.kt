package com.sonusid.ollama.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sonusid.ollama.db.entity.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM Chat")
    fun getAll(): Flow<List<Chat>>


    @Insert
    fun insertAll(vararg chats: Chat)

    @Delete
    fun delete(chat: Chat)
}