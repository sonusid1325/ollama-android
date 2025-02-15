package com.sonusid.ollama.api

import com.sonusid.ollama.db.dao.BaseUrlDao
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    var BASE_URL: String = "localhost:11434/" // Default URL

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit? = null // Make retrofit nullable

    fun initialize(baseUrlDao: BaseUrlDao) {
        runBlocking {
            val baseUrlFromDb = baseUrlDao.getBaseUrl()
            BASE_URL = baseUrlFromDb?.url ?: BASE_URL // Use default if DB is empty

        }

        retrofit = Retrofit.Builder() // Initialize retrofit here
            .baseUrl("http://${BASE_URL}")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: OllamaApiService by lazy {
        retrofit?.create(OllamaApiService::class.java) ?: error("RetrofitClient must be initialized!")
    }
}