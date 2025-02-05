package com.sonusid.ollama.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sonusid.ollama.db.entity.Chat

@Dao
interface ChatDao {
    @Query("SELECT * FROM Chat")
    fun getAll(): List<Chat>

    @Query("SELECT * FROM Chat WHERE cid IN (:ChatIds)")
    fun loadAllByIds(ChatIds: IntArray): List<Chat>


    @Insert
    fun insertAll(vararg chats: Chat)

    @Delete
    fun delete(chat: Chat)
}