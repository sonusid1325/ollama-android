package com.sonusid.ollama.db.repository

import com.sonusid.ollama.db.dao.ChatDao
import com.sonusid.ollama.db.entity.Chat
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {

    val allChats: Flow<List<Chat>> = chatDao.getAllChats()

    suspend fun insert(chat: Chat){
        chatDao.insertChat(chat)
    }

    suspend fun delete(chat: Chat){
        chatDao.deleteChat(chat)
    }
    
}