package com.sonusid.ollama.db.repository

import com.sonusid.ollama.db.dao.BaseUrlDao
import com.sonusid.ollama.db.entity.BaseUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface BaseUrlProvider {
    suspend fun getActiveOrFirst(): BaseUrl?

    suspend fun getAll(): List<BaseUrl>

    suspend fun replaceAll(baseUrls: List<BaseUrl>, refreshActive: Boolean = true)
}

class BaseUrlRepository(private val baseUrlDao: BaseUrlDao) : BaseUrlProvider {
    val activeBaseUrl: StateFlow<String> = activeBaseUrlFlow

    override suspend fun getAll(): List<BaseUrl> = baseUrlDao.getAll()

    override suspend fun getActiveOrFirst(): BaseUrl? = baseUrlDao.getActive() ?: baseUrlDao.getAll().firstOrNull()

    override suspend fun replaceAll(baseUrls: List<BaseUrl>, refreshActive: Boolean) {
        baseUrlDao.replaceBaseUrls(baseUrls)
        if (refreshActive) {
            refreshActiveBaseUrl()
        }
    }

    suspend fun setActive(id: Int) {
        baseUrlDao.setActive(id)
        refreshActiveBaseUrl()
    }

    suspend fun refreshActiveBaseUrl() {
        val activeUrl = getActiveOrFirst()?.url ?: DEFAULT_BASE_URL
        updateActiveBaseUrl(activeUrl)
    }

    fun updateActiveBaseUrl(baseUrl: String) {
        activeBaseUrlState.update { baseUrl.trimEnd('/') }
    }

    private companion object {
        const val DEFAULT_BASE_URL = "http://localhost:11434/"
        val activeBaseUrlState = MutableStateFlow(DEFAULT_BASE_URL.trimEnd('/'))
        val activeBaseUrlFlow: StateFlow<String> = activeBaseUrlState.asStateFlow()
    }
}
