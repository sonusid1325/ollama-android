package com.sonusid.ollama.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonusid.ollama.UiState
import com.sonusid.ollama.api.OllamaRequest
import com.sonusid.ollama.api.OllamaResponse
import com.sonusid.ollama.api.RetrofitClient
import com.sonusid.ollama.api.RetrofitClient.BASE_URL
import com.sonusid.ollama.db.entity.Chat
import com.sonusid.ollama.db.entity.Message
import com.sonusid.ollama.db.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL

data class ModelInfo(val name: String)

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


    fun sendPrompt(prompt: String, model: String?) {
        _uiState.value = UiState.Loading
        val request = OllamaRequest(model = model.toString(), prompt = prompt)

        if (model!=null) {
            RetrofitClient.instance.generateText(request)
                .enqueue(object : Callback<OllamaResponse> {
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
        }else{
            _uiState.value = UiState.Success("Please Choose A model")
        }

        _uiState.value = UiState.Initial
    }

    private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val availableModels: StateFlow<List<ModelInfo>> = _availableModels.asStateFlow()

    fun loadAvailableModels() {

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val url =
                        URL("http://${BASE_URL}/api/tags") // Replace with your Ollama server URL
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val inputStream = connection.inputStream
                    inputStream.bufferedReader().use { it.readText() }
                }

                val jsonArray = JSONObject(response).getJSONArray("models")
                val models = mutableListOf<ModelInfo>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val name = jsonObject.getString("name")
                    models.add(ModelInfo(name))
                }
                _availableModels.value = models

            } catch (e: Exception) {
                Log.e("OllamaError", "Error loading models: ${e.message}")
                _availableModels.value = listOf<ModelInfo>(ModelInfo(e.message.toString()))
            }
        }
        _uiState.value = UiState.Initial
    }


    fun insertChat(chat: Chat) = viewModelScope.launch {
        repository.newChat(chat)
    }

    fun insert(message: Message) = viewModelScope.launch {
        repository.insert(message)
    }

    fun delete(message: Message) = viewModelScope.launch {
        repository.delete(message)
    }

}