package com.sonusid.ollama.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonusid.ollama.db.entity.User
import com.sonusid.ollama.db.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import android.util.Log
import com.sonusid.ollama.UiState
import com.sonusid.ollama.api.OllamaRequest
import com.sonusid.ollama.api.OllamaResponse
import com.sonusid.ollama.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OllamaViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uiState : MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    var allUsers: Flow<List<User>> = repository.allUsers

    fun sendPrompt(prompt: String){
        _uiState.value = UiState.Loading

        val request = OllamaRequest(model = "qwen2.5-coder:0.5b", prompt = prompt)

        RetrofitClient.instance.generateText(request).enqueue(object : Callback<OllamaResponse> {
            override fun onResponse(call: Call<OllamaResponse>, response: Response<OllamaResponse>) {
                if (response.isSuccessful) {
                    response.body()?.response?.let{
                        output->
                        _uiState.value = UiState.Success(output)}

                } else {
                    response.errorBody()?.string()?.let { error ->
                        _uiState.value = UiState.Error(error)
                    }
                }
            }

            override fun onFailure(call: Call<OllamaResponse>, t: Throwable) {
                Log.e("OllamaError", "Request failed: ${t.message}")
                t.message?.let{ error ->
                    _uiState.value = UiState.Error(error)
                }
            }
        })

        _uiState.value = UiState.Initial
    }

    fun generateOllamaText(prompt: String) {
        val request = OllamaRequest(model = "qwen2.5-coder:0.5b", prompt = prompt)

        RetrofitClient.instance.generateText(request).enqueue(object : Callback<OllamaResponse> {
            override fun onResponse(call: Call<OllamaResponse>, response: Response<OllamaResponse>) {
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

    fun insert(user: User) = viewModelScope.launch {
        repository.insert(user)
    }

    fun delete(user: User) = viewModelScope.launch {
        repository.delete(user)
    }


}