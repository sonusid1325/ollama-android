package com.sonusid.ollama.db.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonusid.ollama.db.entity.Chat
import com.sonusid.ollama.db.repo.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    val allChats: Flow<List<Chat>> = repository.allChats

    fun insert(user: Chat) = viewModelScope.launch {
        repository.insert(user)
    }

    fun delete(user: Chat) = viewModelScope.launch {
        repository.delete(user)
    }
}