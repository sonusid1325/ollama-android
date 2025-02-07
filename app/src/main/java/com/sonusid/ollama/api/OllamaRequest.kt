package com.sonusid.ollama.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call

// Define the request body model
data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

// Define the response model
data class OllamaResponse(
    val response: String
)

// Retrofit API interface
interface OllamaApiService {
    @Headers("Content-Type: application/json")
    @POST("api/generate")
    fun generateText(@Body request: OllamaRequest): Call<OllamaResponse>
}
