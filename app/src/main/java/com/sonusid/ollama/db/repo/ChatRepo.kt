package com.sonusid.ollama.db.repo

import com.sonusid.ollama.db.dao.ChatDao
import com.sonusid.ollama.db.entity.Chat

class ChatRepository(private val userDao: ChatDao) {
    val allChats = userDao.getAll()

    suspend fun insert(user: Chat) {
        userDao.insertAll(user)
    }

    suspend fun delete(user: Chat) {
        userDao.delete(user)
    }
}