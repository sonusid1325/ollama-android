package com.sonusid.ollama

import com.sonusid.ollama.db.entity.User

interface UserDataSource {
    suspend fun getUsers(): List<User>
    suspend fun addUser(name: String, age: Int)
    suspend fun deleteUser(user: User)
}
