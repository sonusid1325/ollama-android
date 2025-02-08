package com.sonusid.ollama.db.dao

import androidx.room.*
import com.sonusid.ollama.db.entity.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)

    @Query("SELECT * FROM chat_table")
    fun getAllChats(): Flow<List<Chat>>

    @Delete
    suspend fun deleteChat(chat: Chat)
}