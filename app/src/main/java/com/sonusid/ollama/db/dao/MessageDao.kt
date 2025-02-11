package com.sonusid.ollama.db.dao

import androidx.room.*
import com.sonusid.ollama.db.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(message: Message)

    @Query("SELECT * FROM chat_table")
    fun getAllChats(): Flow<List<Message>>


    @Delete
    suspend fun deleteChat(message: Message)
}