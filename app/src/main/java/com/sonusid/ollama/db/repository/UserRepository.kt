package com.sonusid.ollama.db.repository

import com.sonusid.ollama.db.dao.ChatDao
import com.sonusid.ollama.db.entity.Chat
import kotlinx.coroutines.flow.Flow

class UserRepository(private val chatDao: ChatDao) {

    val allUsers: Flow<List<Chat>> = chatDao.getAllUsers()

    suspend fun insert(chat: Chat) {
        chatDao.insertUser(chat)
    }

    suspend fun delete(chat: Chat) {
        chatDao.deleteUser(chat)
    }
}