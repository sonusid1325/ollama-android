package com.sonusid.ollama.db.repository

import com.sonusid.ollama.db.dao.UserDao
import com.sonusid.ollama.db.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun insert(user: User) {
        userDao.insertUser(user)
    }

    suspend fun delete(user: User) {
        userDao.deleteUser(user)
    }
}