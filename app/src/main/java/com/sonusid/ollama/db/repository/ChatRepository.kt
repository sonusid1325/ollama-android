package com.sonusid.ollama.db.repository

import com.sonusid.ollama.db.dao.MessageDao
import com.sonusid.ollama.db.entity.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val messageDao: MessageDao) {

    val allChats: Flow<List<Message>> = messageDao.getAllChats()

    suspend fun insert(message: Message){
        messageDao.insertChat(message)
    }

    suspend fun delete(message: Message){
        messageDao.deleteChat(message)
    }
    
}