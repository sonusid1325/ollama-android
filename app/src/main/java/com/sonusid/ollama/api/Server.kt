package com.sonusid.ollama.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.116.25:11434/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)  // Increase connection timeout
        .readTimeout(120, TimeUnit.SECONDS)  // Increase read timeout
        .writeTimeout(120, TimeUnit.SECONDS)  // Increase write timeout
        .build()

    val instance: OllamaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)  // Use the custom client with timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApiService::class.java)
    }
}
