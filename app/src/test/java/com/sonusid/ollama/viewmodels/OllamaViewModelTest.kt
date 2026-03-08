package com.sonusid.ollama.viewmodels

import com.sonusid.ollama.api.RetrofitClient
import com.sonusid.ollama.db.entity.BaseUrl
import com.sonusid.ollama.db.dao.ChatDao
import com.sonusid.ollama.db.dao.MessageDao
import com.sonusid.ollama.db.dao.ModelPreferenceDao
import com.sonusid.ollama.db.entity.Chat
import com.sonusid.ollama.db.entity.Message
import com.sonusid.ollama.db.entity.SelectedModel
import com.sonusid.ollama.db.repository.BaseUrlProvider
import com.sonusid.ollama.db.repository.ChatRepository
import com.sonusid.ollama.db.repository.ModelPreferenceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class OllamaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeChatDao: FakeChatDao
    private lateinit var fakeMessageDao: FakeMessageDao
    private lateinit var fakeModelPreferenceDao: FakeModelPreferenceDao

    private lateinit var chatRepository: ChatRepository
    private lateinit var modelPreferenceRepository: ModelPreferenceRepository
    private lateinit var baseUrlFlow: MutableStateFlow<String>

    @Before
    fun setUp() {
        fakeChatDao = FakeChatDao()
        fakeMessageDao = FakeMessageDao()
        fakeModelPreferenceDao = FakeModelPreferenceDao()
        chatRepository = ChatRepository(fakeMessageDao, fakeChatDao)
        modelPreferenceRepository = ModelPreferenceRepository(fakeModelPreferenceDao)
        baseUrlFlow = MutableStateFlow(DEFAULT_BASE_URL.trimEnd('/'))
    }

    private fun createViewModel(initialSelectedModel: String? = null): OllamaViewModel =
        OllamaViewModel(
            chatRepository = chatRepository,
            modelPreferenceRepository = modelPreferenceRepository,
            initialSelectedModel = initialSelectedModel,
            baseUrlFlow = baseUrlFlow,
            shouldAutoLoadModels = false
        )

    private suspend fun setBaseUrl(url: String): String {
        RetrofitClient.initialize(FakeBaseUrlProvider(url))
        val normalized = RetrofitClient.currentBaseUrl().trimEnd('/')
        baseUrlFlow.value = normalized
        return normalized
    }

    @Test
    fun `refreshSelectedModel persists single model selection`() =
        runTest {
            val baseUrl = setBaseUrl(DEFAULT_BASE_URL)
            val viewModel = createViewModel()

            viewModel.refreshSelectedModel(listOf(ModelInfo("mistral")))

            assertEquals("mistral", viewModel.selectedModel.value)
            assertEquals("mistral", fakeModelPreferenceDao.getByBaseUrl(baseUrl)?.modelName)
        }

    @Test
    fun `refreshSelectedModel clears selection when saved and current are unavailable`() = runTest {
        val baseUrl = setBaseUrl(DEFAULT_BASE_URL)
        val viewModel = createViewModel()
        viewModel.applyInitialSelectedModel("current-model")
        modelPreferenceRepository.setSelectedModel(baseUrl, "saved-model")

        viewModel.refreshSelectedModel(listOf(ModelInfo("new-model-1"), ModelInfo("new-model-2")))

        assertNull(viewModel.selectedModel.value)
        assertNull(fakeModelPreferenceDao.getByBaseUrl(baseUrl))
    }

    @Test
    fun `switching between single and multiple model servers updates selection per base url`() = runTest {
        val singleBaseUrl = setBaseUrl("http://single.example.com/")
        val viewModel = createViewModel()

        viewModel.refreshSelectedModel(listOf(ModelInfo("single-model")))
        assertEquals("single-model", viewModel.selectedModel.value)
        assertEquals("single-model", fakeModelPreferenceDao.getByBaseUrl(singleBaseUrl)?.modelName)

        val multiBaseUrl = setBaseUrl("http://multi.example.com/")
        viewModel.refreshSelectedModel(listOf(ModelInfo("mistral"), ModelInfo("llama2")))

        assertNull(viewModel.selectedModel.value)
        assertNull(fakeModelPreferenceDao.getByBaseUrl(multiBaseUrl))

        viewModel.updateSelectedModel("mistral")
        assertEquals("mistral", viewModel.selectedModel.value)
        assertEquals("mistral", fakeModelPreferenceDao.getByBaseUrl(multiBaseUrl)?.modelName)

        setBaseUrl(singleBaseUrl)
        viewModel.refreshSelectedModel(listOf(ModelInfo("single-model")))
        assertEquals("single-model", viewModel.selectedModel.value)
        assertEquals("single-model", fakeModelPreferenceDao.getByBaseUrl(singleBaseUrl)?.modelName)

        setBaseUrl(multiBaseUrl)
        viewModel.refreshSelectedModel(listOf(ModelInfo("mistral"), ModelInfo("qwen2")))
        assertEquals("mistral", viewModel.selectedModel.value)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val testDispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description) {
        super.starting(description)
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        kotlinx.coroutines.Dispatchers.resetMain()
    }
}

private class FakeModelPreferenceDao : ModelPreferenceDao {
    private val storage = mutableMapOf<String, SelectedModel>()

    override suspend fun getByBaseUrl(baseUrl: String): SelectedModel? = storage[baseUrl]

    override suspend fun upsert(model: SelectedModel) {
        storage[model.baseUrl] = model
    }

    override suspend fun deleteByBaseUrl(baseUrl: String) {
        storage.remove(baseUrl)
    }
}

private class FakeBaseUrlProvider(initialUrl: String) : BaseUrlProvider {
    private var urls = mutableListOf(BaseUrl(url = initialUrl, isActive = true))

    override suspend fun getActiveOrFirst(): BaseUrl? = urls.firstOrNull { it.isActive } ?: urls.firstOrNull()

    override suspend fun getAll(): List<BaseUrl> = urls

    override suspend fun replaceAll(baseUrls: List<BaseUrl>) {
        urls = baseUrls.toMutableList()
    }
}

private const val DEFAULT_BASE_URL = "http://localhost:11434/"

private class FakeChatDao : ChatDao {
    private val chatsFlow = MutableStateFlow<List<Chat>>(emptyList())

    override suspend fun insertChat(chat: Chat) {
        chatsFlow.value = chatsFlow.value + chat
    }

    override fun getAllChats(): Flow<List<Chat>> = chatsFlow

    override suspend fun deleteChat(chat: Chat) {
        chatsFlow.value = chatsFlow.value.filterNot { it.chatId == chat.chatId }
    }
}

private class FakeMessageDao : MessageDao {
    override suspend fun insertMessage(message: Message) = Unit

    override fun getAllMessages(chatId: Int): Flow<List<Message>> = MutableStateFlow(emptyList())

    override suspend fun deleteMessage(message: Message) = Unit
}
