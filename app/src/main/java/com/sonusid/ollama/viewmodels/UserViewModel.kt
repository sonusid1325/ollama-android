package com.sonusid.ollama.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonusid.ollama.db.entity.User
import com.sonusid.ollama.db.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    val allUsers: Flow<List<User>> = repository.allUsers

    fun insert(user: User) = viewModelScope.launch {
        repository.insert(user)
    }

    fun delete(user: User) = viewModelScope.launch {
        repository.delete(user)
    }
}