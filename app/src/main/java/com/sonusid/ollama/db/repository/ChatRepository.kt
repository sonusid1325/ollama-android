package com.sonusid.ollama.db.repository

import com.sonusid.ollama.db.dao.BaseUrlDao
import com.sonusid.ollama.db.dao.ChatDao
import com.sonusid.ollama.db.dao.MessageDao
import com.sonusid.ollama.db.entity.Chat
import com.sonusid.ollama.db.entity.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val messageDao: MessageDao, private val chatDao: ChatDao) {

    val allChats: Flow<List<Chat>> = chatDao.getAllChats()

    fun getMessages(chatId: Int) = messageDao.getAllMessages(chatId)

    suspend fun newChat(chat: Chat){
        chatDao.insertChat(chat)
    }

    suspend fun deleteChat(chat: Chat){
        chatDao.deleteChat(chat)
    }

    suspend fun insert(message: Message){
        messageDao.insertMessage(message)
    }

    suspend fun delete(message: Message){
        messageDao.deleteMessage(message)
    }
    
}