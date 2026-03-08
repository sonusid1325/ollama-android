package com.sonusid.ollama.api

import com.sonusid.ollama.db.entity.BaseUrl
import com.sonusid.ollama.db.repository.BaseUrlProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetrofitClientTest {
    @Test
    fun `initialize falls back to default when no stored urls`() = runBlocking {
        val provider = FakeBaseUrlProvider()

        val state = RetrofitClient.initialize(provider)

        assertEquals("http://localhost:11434/", state.baseUrl)
        assertTrue(state.usedFallback)
        assertEquals(listOf("http://localhost:11434/"), provider.storedUrls())
        assertTrue(provider.activeUrlIs(state.baseUrl))
    }

    @Test
    fun `initialize prefers active valid url and removes invalid entries`() = runBlocking {
        val provider = FakeBaseUrlProvider(
            mutableListOf(
                BaseUrl(id = 1, url = "http://valid-host:8080", isActive = true),
                BaseUrl(id = 2, url = "http://bad host", isActive = false)
            )
        )

        val state = RetrofitClient.initialize(provider)

        assertFalse(state.usedFallback)
        assertEquals("http://valid-host:8080/", state.baseUrl)
        assertEquals(listOf("http://valid-host:8080/"), provider.storedUrls())
        assertTrue(provider.activeUrlIs(state.baseUrl))
    }

    @Test
    fun `initialize promotes first valid url when active is missing`() = runBlocking {
        val provider = FakeBaseUrlProvider(
            mutableListOf(
                BaseUrl(id = 1, url = "http://first-valid:11434", isActive = false),
                BaseUrl(id = 2, url = "http://second-valid:11435", isActive = false)
            )
        )

        val state = RetrofitClient.initialize(provider)

        assertTrue(state.usedFallback)
        assertEquals("http://first-valid:11434/", state.baseUrl)
        assertEquals(listOf("http://first-valid:11434/", "http://second-valid:11435/"), provider.storedUrls())
        assertTrue(provider.activeUrlIs(state.baseUrl))
    }
}

private class FakeBaseUrlProvider(private val items: MutableList<BaseUrl> = mutableListOf()) : BaseUrlProvider {
    private var nextId: Int = (items.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun getActiveOrFirst(): BaseUrl? = items.firstOrNull { it.isActive } ?: items.firstOrNull()

    override suspend fun getAll(): List<BaseUrl> = items.toList()

    override suspend fun replaceAll(baseUrls: List<BaseUrl>, refreshActive: Boolean) {
        items.clear()
        baseUrls.forEach { baseUrl ->
            val id = if (baseUrl.id == 0) nextId++ else baseUrl.id
            items.add(baseUrl.copy(id = id))
        }
        if (refreshActive && items.isNotEmpty() && items.none { it.isActive }) {
            items[0] = items[0].copy(isActive = true)
        }
    }

    fun storedUrls(): List<String> = items.map { it.url }

    fun activeUrlIs(url: String): Boolean = items.firstOrNull { it.isActive }?.url?.trimEnd('/') == url.trimEnd('/')
}
