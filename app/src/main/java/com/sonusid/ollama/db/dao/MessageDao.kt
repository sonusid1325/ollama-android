package com.sonusid.ollama.db.dao

import androidx.room.*
import com.sonusid.ollama.db.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM chat_table WHERE chatId = :chatId ")
    fun getAllMessages(chatId: Int): Flow<List<Message>>


    @Delete
    suspend fun deleteMessage(message: Message)
}