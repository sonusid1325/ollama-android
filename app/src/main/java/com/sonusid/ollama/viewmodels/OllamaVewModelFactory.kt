package com.sonusid.ollama.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sonusid.ollama.db.repository.UserRepository

class OllamaVewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OllamaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OllamaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}