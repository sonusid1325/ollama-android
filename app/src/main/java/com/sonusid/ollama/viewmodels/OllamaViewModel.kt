package com.sonusid.ollama.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonusid.ollama.UiState
import com.sonusid.ollama.api.OllamaRequest
import com.sonusid.ollama.api.OllamaResponse
import com.sonusid.ollama.api.RetrofitClient
import com.sonusid.ollama.db.entity.Chat
import com.sonusid.ollama.db.entity.Message
import com.sonusid.ollama.db.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OllamaViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    init {
        viewModelScope.launch {
            repository.allChats.collect {
                _chats.value = it
            }
        }
    }

    fun allMessages(chatId: Int): Flow<List<Message>> = repository.getMessages(chatId)



    fun sendPrompt(prompt: String) {
        _uiState.value = UiState.Loading


//        val request = OllamaRequest(model = "llama3.2", prompt = prompt)
        val request = OllamaRequest(model = "qwen2.5-coder:0.5b", prompt = prompt)

        RetrofitClient.instance.generateText(request).enqueue(object : Callback<OllamaResponse> {
            override fun onResponse(
                call: Call<OllamaResponse>,
                response: Response<OllamaResponse>,
            ) {
                if (response.isSuccessful) {
                    response.body()?.response?.let { output ->
                        _uiState.value = UiState.Success(output)
                    }

                } else {
                    response.errorBody()?.string()?.let { error ->
                        _uiState.value = UiState.Error(error)
                    }
                }
            }

            override fun onFailure(call: Call<OllamaResponse>, t: Throwable) {
                Log.e("OllamaError", "Request failed: ${t.message}")
                t.message?.let { error ->
                    _uiState.value = UiState.Error(error)
                }
            }
        })

        _uiState.value = UiState.Initial
    }

    fun generateOllamaText(prompt: String) {
        val request = OllamaRequest(model = "qwen2.5-coder:0.5b", prompt = prompt)

        RetrofitClient.instance.generateText(request).enqueue(object : Callback<OllamaResponse> {
            override fun onResponse(
                call: Call<OllamaResponse>,
                response: Response<OllamaResponse>,
            ) {
                if (response.isSuccessful) {
                    Log.d("OllamaResponse", "Generated: ${response.body()?.response}")

                } else {
                    Log.e("OllamaError", "Failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<OllamaResponse>, t: Throwable) {
                Log.e("OllamaError", "Request failed: ${t.message}")
            }
        })
    }

    fun insert(message: Message) = viewModelScope.launch {
        repository.insert(message)
    }

    fun delete(message: Message) = viewModelScope.launch {
        repository.delete(message)
    }
}