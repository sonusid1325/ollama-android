package com.sonusid.ollama.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonusid.ollama.db.entity.User
import com.sonusid.ollama.db.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import android.util.Log
import com.sonusid.ollama.api.OllamaRequest
import com.sonusid.ollama.api.OllamaResponse
import com.sonusid.ollama.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    val allUsers: Flow<List<User>> = repository.allUsers

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