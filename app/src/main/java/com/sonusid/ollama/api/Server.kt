package com.sonusid.ollama.api

import com.sonusid.ollama.db.entity.BaseUrl
import com.sonusid.ollama.db.repository.BaseUrlProvider
import com.sonusid.ollama.db.repository.ModelPreferenceRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import com.sonusid.ollama.util.normalizeUrlInput
import com.sonusid.ollama.util.validateUrlFormat
import java.util.concurrent.TimeUnit
import java.net.URL

data class BaseUrlInitializationState(
    val baseUrl: String,
    val usedFallback: Boolean,
    val errorMessage: String? = null
)

object RetrofitClient {
    private const val DEFAULT_BASE_URL = "http://localhost:11434/" // Default URL
    private var baseUrl: String = DEFAULT_BASE_URL
    private var lastInitializationState: BaseUrlInitializationState? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit? = null
    private val retrofitMutex = Mutex()

    suspend fun initialize(baseUrlProvider: BaseUrlProvider, modelPreferenceRepository: ModelPreferenceRepository? = null): BaseUrlInitializationState {
        return retrofitMutex.withLock {
            val state = resolveBaseUrl(baseUrlProvider)
            if (retrofit == null || baseUrl != state.baseUrl) {
                baseUrl = state.baseUrl
                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            lastInitializationState = state
            modelPreferenceRepository?.pruneMissingBaseUrls(getAllBaseUrls(baseUrlProvider))
            state
        }
    }

    suspend fun refreshBaseUrl(baseUrlProvider: BaseUrlProvider, modelPreferenceRepository: ModelPreferenceRepository? = null): BaseUrlInitializationState =
        initialize(baseUrlProvider, modelPreferenceRepository)

    fun currentBaseUrl(): String = baseUrl

    fun getLastInitializationState(): BaseUrlInitializationState? = lastInitializationState

    val instance: OllamaApiService
        get() = retrofit?.create(OllamaApiService::class.java)
            ?: error("RetrofitClient must be initialized!")

    private fun normalizeBaseUrl(activeUrl: String?): String {
        val normalizedInput = normalizeUrlInput(activeUrl ?: "")
        val cleanedUrl = normalizedInput.trimEnd('/').takeIf { it.isNotBlank() }
            ?: DEFAULT_BASE_URL.trimEnd('/')
        val withScheme = if (cleanedUrl.startsWith("http://") || cleanedUrl.startsWith("https://")) {
            cleanedUrl
        } else {
            "http://$cleanedUrl"
        }
        return "$withScheme/"
    }

    private fun isValidBaseUrl(url: String): Boolean {
        val validation = validateUrlFormat(url)
        if (!validation.isValid) return false
        val normalized = validation.normalizedUrl
        return normalized.toHttpUrlOrNull() != null && runCatching {
            URL(normalized)
        }.isSuccess
    }

    private suspend fun resolveBaseUrl(baseUrlProvider: BaseUrlProvider): BaseUrlInitializationState {
        val storedBaseUrls = runCatching { baseUrlProvider.getAll() }.getOrDefault(emptyList())
        if (storedBaseUrls.isEmpty()) {
            val defaultEntry = listOf(BaseUrl(url = DEFAULT_BASE_URL, isActive = true))
            baseUrlProvider.replaceAll(defaultEntry)
            return BaseUrlInitializationState(
                baseUrl = DEFAULT_BASE_URL,
                usedFallback = true,
                errorMessage = "ベースURLが未設定のためデフォルトにフォールバックしました"
            )
        }

        val normalizedEntries = storedBaseUrls.map { it.copy(url = normalizeBaseUrl(it.url)) }
        val validEntries = normalizedEntries.filter { entry -> isValidBaseUrl(entry.url) }
        val invalidEntries = normalizedEntries.filterNot { entry -> isValidBaseUrl(entry.url) }
        val activeValidEntry = validEntries.firstOrNull { it.isActive }
        val selectedEntry = activeValidEntry ?: validEntries.firstOrNull()

        val usedFallback = selectedEntry == null || validEntries.size != normalizedEntries.size || activeValidEntry == null
        val finalBaseUrl = selectedEntry?.url ?: DEFAULT_BASE_URL
        val errorMessage = when {
            selectedEntry == null -> "保存されたベースURLが無効のためデフォルトにフォールバックしました"
            invalidEntries.isNotEmpty() -> "無効なベースURLを除去し有効なURLに切り替えました"
            activeValidEntry == null -> "有効なアクティブURLがないため利用可能なURLに切り替えました"
            else -> null
        }

        if (usedFallback) {
            val sanitizedList = if (selectedEntry != null) {
                validEntries.map { entry ->
                    entry.copy(isActive = entry.id == selectedEntry.id)
                }.ifEmpty { listOf(selectedEntry.copy(isActive = true)) }
            } else {
                listOf(BaseUrl(url = DEFAULT_BASE_URL, isActive = true))
            }
            baseUrlProvider.replaceAll(sanitizedList)
            val logMessage = errorMessage ?: "ベースURLの状態を更新しました"
            Log.e(
                "RetrofitClient",
                "$logMessage: 使用中=${finalBaseUrl}, 無効=${invalidEntries.joinToString { it.url }}"
            )
        }

        return BaseUrlInitializationState(
            baseUrl = finalBaseUrl,
            usedFallback = usedFallback,
            errorMessage = if (usedFallback) errorMessage else null
        )
    }

    private suspend fun getAllBaseUrls(baseUrlProvider: BaseUrlProvider): List<String> {
        return baseUrlProvider.getAll().map { it.url.trimEnd('/') }
    }
}
