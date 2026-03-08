package com.sonusid.ollama.ui.screens.settings

import com.sonusid.ollama.api.BaseUrlInitializationState
import com.sonusid.ollama.db.dao.BaseUrlDao
import com.sonusid.ollama.db.entity.BaseUrl
import com.sonusid.ollama.db.repository.BaseUrlRepository
import com.sonusid.ollama.db.repository.ModelPreferenceRepository
import com.sonusid.ollama.db.entity.SelectedModel
import com.sonusid.ollama.db.dao.ModelPreferenceDao
import com.sonusid.ollama.util.UrlValidationResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTest {
    @Test
    fun `validateActiveConnections checks active inputs only`() = runBlocking {
        val activeInput = ServerInput(localId = "active", url = "http://active:11434", isActive = true)
        val inactiveInput = ServerInput(localId = "inactive", url = "http://inactive:11434", isActive = false)
        val checkedUrls = mutableListOf<String>()

        val result = validateActiveConnections(listOf(activeInput, inactiveInput)) { url ->
            checkedUrls += url
            UrlValidationResult(normalizedUrl = url, isValid = url != inactiveInput.url)
        }

        assertEquals(listOf(activeInput.url), checkedUrls)
        assertEquals(mapOf("active" to false), result)
        assertFalse(result.containsKey(inactiveInput.localId))
    }

    @Test
    fun `saveServers refreshes client and updates active base url`() = runBlocking {
        val baseUrlDao = FakeBaseUrlDao()
        val baseUrlRepository = BaseUrlRepository(baseUrlDao)
        val modelPreferenceRepository = ModelPreferenceRepository(FakeModelPreferenceDao())
        val inputsToSave = listOf(
            BaseUrl(id = 0, url = "http://primary:1234", isActive = true),
            BaseUrl(id = 0, url = "http://secondary:1234", isActive = false)
        )
        var refreshCalled = 0

        val state = saveServers(
            inputsToSave,
            baseUrlRepository,
            modelPreferenceRepository
        ) { repo, modelRepo ->
            refreshCalled++
            assertSame(baseUrlRepository, repo)
            assertSame(modelPreferenceRepository, modelRepo)
            BaseUrlInitializationState(baseUrl = "http://primary:1234", usedFallback = false)
        }

        assertEquals(1, refreshCalled)
        assertEquals("http://primary:1234", state.baseUrl)
        assertEquals("http://primary:1234", baseUrlRepository.activeBaseUrl.value)
        assertEquals(2, baseUrlDao.baseUrls.size)
        assertTrue(baseUrlDao.baseUrls.first { it.url == "http://primary:1234" }.isActive)
        assertFalse(baseUrlDao.baseUrls.first { it.url == "http://secondary:1234" }.isActive)
    }

    @Test
    fun `saveServers updates active flow after retrofit client refresh`() = runBlocking {
        val baseUrlDao = FakeBaseUrlDao()
        val baseUrlRepository = BaseUrlRepository(baseUrlDao)
        val modelPreferenceRepository = ModelPreferenceRepository(FakeModelPreferenceDao())
        val inputsToSave = listOf(
            BaseUrl(id = 0, url = "http://primary:1234", isActive = true),
            BaseUrl(id = 0, url = "http://secondary:1234", isActive = false)
        )
        val retrofitClient = StubRetrofitClient()

        val state = saveServers(
            inputsToSave,
            baseUrlRepository,
            modelPreferenceRepository
        ) { repo, modelRepo ->
            retrofitClient.refreshBaseUrl(repo, modelRepo)
        }

        assertEquals(1, retrofitClient.refreshCalls)
        assertEquals("http://primary:1234", state.baseUrl)
        assertEquals("http://primary:1234", baseUrlRepository.activeBaseUrl.value)
        assertEquals(baseUrlRepository.activeBaseUrl.value, retrofitClient.currentBaseUrl())
    }
}

private class FakeBaseUrlDao : BaseUrlDao {
    val baseUrls = mutableListOf<BaseUrl>()
    private var nextId = 1

    override suspend fun getAll(): List<BaseUrl> = baseUrls.toList()

    override suspend fun getActive(): BaseUrl? = baseUrls.firstOrNull { it.isActive }

    override suspend fun insert(baseUrl: BaseUrl): Long {
        val id = if (baseUrl.id != 0) baseUrl.id else nextId++
        val toStore = baseUrl.copy(id = id)
        baseUrls.removeAll { it.id == id }
        baseUrls.add(toStore)
        return id.toLong()
    }

    override suspend fun insertAll(baseUrls: List<BaseUrl>): List<Long> = baseUrls.map { insert(it) }

    override suspend fun update(baseUrl: BaseUrl) {
        insert(baseUrl)
    }

    override suspend fun delete(baseUrl: BaseUrl) {
        baseUrls.removeIf { it.id == baseUrl.id }
    }

    override suspend fun deleteById(id: Int) {
        baseUrls.removeIf { it.id == id }
    }

    override suspend fun clear() {
        baseUrls.clear()
    }

    override suspend fun clearActive() {
        baseUrls.replaceAll { it.copy(isActive = false) }
    }

    override suspend fun activateById(id: Int) {
        baseUrls.replaceAll { it.copy(isActive = it.id == id) }
    }

    override suspend fun setActive(id: Int) {
        clearActive()
        activateById(id)
    }

    override suspend fun replaceBaseUrls(baseUrls: List<BaseUrl>) {
        clear()
        val insertedIds = insertAll(baseUrls.map { it.copy(isActive = false) })
        val activeIndex = baseUrls.indexOfFirst { it.isActive }
        val activeId = when {
            activeIndex in insertedIds.indices -> insertedIds[activeIndex].toInt()
            insertedIds.isNotEmpty() -> insertedIds.first().toInt()
            else -> null
        }
        activeId?.let { activateById(it) }
    }
}

private class FakeModelPreferenceDao : ModelPreferenceDao {
    override suspend fun getByBaseUrl(baseUrl: String): SelectedModel? = null

    override suspend fun upsert(model: SelectedModel) {}

    override suspend fun deleteByBaseUrl(baseUrl: String) {}

    override suspend fun getAllBaseUrls(): List<String> = emptyList()

    override suspend fun deleteAllExcept(baseUrls: List<String>) {}

    override suspend fun clearAll() {}
}

private class StubRetrofitClient {
    private var currentBaseUrl: String = "http://stub-initial:11434"
    var refreshCalls: Int = 0
        private set

    suspend fun refreshBaseUrl(
        baseUrlRepository: BaseUrlRepository,
        modelPreferenceRepository: ModelPreferenceRepository
    ): BaseUrlInitializationState {
        refreshCalls++
        val activeUrl = baseUrlRepository.getActiveOrFirst()?.url ?: currentBaseUrl
        val normalized = activeUrl.trimEnd('/')
        currentBaseUrl = normalized
        return BaseUrlInitializationState(baseUrl = normalized, usedFallback = false)
    }

    fun currentBaseUrl(): String = currentBaseUrl
}
