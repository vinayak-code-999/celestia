package ai.moon.celestia.feature.chat

import ai.moon.celestia.core.data.ChatStorageManager
import ai.moon.celestia.core.data.ChatSessionDto
import ai.moon.celestia.core.data.CelestiaTaskManager
import ai.moon.celestia.core.data.OllamaRepository
import ai.moon.celestia.core.datastore.UserPreferencesRepository
import android.content.Context
import ai.moon.celestia.core.designsystem.util.StreamingTextBuffer
import ai.moon.celestia.core.model.OllamaChatMessage
import ai.moon.celestia.core.model.OllamaErrorType
import ai.moon.celestia.core.model.OllamaModelItem
import ai.moon.celestia.core.network.OllamaHardwareException
import ai.moon.celestia.core.util.VoiceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class UiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokensPerSec: Float = 0f,
    val images: List<String> = emptyList()
)

sealed interface ChatUiState {
    data object Idle : ChatUiState
    data class WarmingUp(val modelName: String) : ChatUiState
    data class Streaming(val currentTokensPerSec: Float) : ChatUiState
    data object Completed : ChatUiState
    data class Error(val errorType: OllamaErrorType) : ChatUiState
    data class SearchingWeb(val statusMessage: String) : ChatUiState
    data class ScrapeAndExtract(val urlCount: Int) : ChatUiState
    data class Synthesizing(val sourceCount: Int) : ChatUiState
}

class ChatViewModel(
    context: Context,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context),
    private val repository: OllamaRepository = OllamaRepository(),
    private val storageManager: ChatStorageManager = ChatStorageManager(context)
) : ViewModel() {

    val voiceManager = VoiceManager(context)

    private val textBuffer = StreamingTextBuffer(viewModelScope, flushIntervalMs = 50L)

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChatSessionDto>>(emptyList())
    val sessions: StateFlow<List<ChatSessionDto>> = _sessions.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSessionIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedSessionIds: StateFlow<Set<String>> = _selectedSessionIds.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _isWebSearchEnabled = MutableStateFlow(false)
    val isWebSearchEnabled: StateFlow<Boolean> = _isWebSearchEnabled.asStateFlow()

    private val webSearchEngine = ai.moon.celestia.core.network.WebSearchEngine()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _isFilterVisible = MutableStateFlow(false)
    val isFilterVisible: StateFlow<Boolean> = _isFilterVisible.asStateFlow()

    fun toggleFilters() {
        _isFilterVisible.value = !_isFilterVisible.value
    }

    private val _matchingSessionIds = MutableStateFlow<Set<String>>(emptySet())
    val matchingSessionIds: StateFlow<Set<String>> = _matchingSessionIds.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val messages: StateFlow<List<UiChatMessage>> = _messages.asStateFlow()

    private val _activeModel = MutableStateFlow(preferencesRepository.userPreferencesFlow.value.defaultModel)
    val activeModel: StateFlow<String> = _activeModel.asStateFlow()

    private val _availableModels = MutableStateFlow<List<OllamaModelItem>>(emptyList())
    val availableModels: StateFlow<List<OllamaModelItem>> = _availableModels.asStateFlow()

    private val _maxContextTokens = MutableStateFlow(preferencesRepository.userPreferencesFlow.value.maxContextTokens)
    val maxContextTokensState: StateFlow<Int> = _maxContextTokens.asStateFlow()

    private var hostUrl = preferencesRepository.userPreferencesFlow.value.hostUrl
    private var maxContextTokens = preferencesRepository.userPreferencesFlow.value.maxContextTokens
    private var autoUnloadModel = preferencesRepository.userPreferencesFlow.value.autoUnloadModel
    private var numThreads = preferencesRepository.userPreferencesFlow.value.numThreads
    private var numGpuLayers = preferencesRepository.userPreferencesFlow.value.numGpuLayers
    private var activeStreamingJob: kotlinx.coroutines.Job? = null

    init {
        val initialPrefs = preferencesRepository.userPreferencesFlow.value
        hostUrl = initialPrefs.hostUrl
        _activeModel.value = initialPrefs.defaultModel
        maxContextTokens = initialPrefs.maxContextTokens
        _maxContextTokens.value = initialPrefs.maxContextTokens
        autoUnloadModel = initialPrefs.autoUnloadModel
        numThreads = initialPrefs.numThreads
        numGpuLayers = initialPrefs.numGpuLayers

        viewModelScope.launch(Dispatchers.IO) {
            _sessions.value = storageManager.loadSessions()
        }

        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                hostUrl = prefs.hostUrl
                _activeModel.value = prefs.defaultModel
                maxContextTokens = prefs.maxContextTokens
                _maxContextTokens.value = prefs.maxContextTokens
                autoUnloadModel = prefs.autoUnloadModel
                numThreads = prefs.numThreads
                numGpuLayers = prefs.numGpuLayers
            }
        }
        loadAvailableModels()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.cleanup()
    }

    fun updateHostUrl(newUrl: String) {
        hostUrl = newUrl
        loadAvailableModels()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                _matchingSessionIds.value = emptySet()
            } else {
                _matchingSessionIds.value = storageManager.searchMessagesForQuery(query)
            }
        }
    }

    fun setSearchVisible(visible: Boolean) {
        _isSearchVisible.value = visible
        if (!visible) {
            updateSearchQuery("")
        }
    }

    fun toggleWebSearch() {
        _isWebSearchEnabled.value = !_isWebSearchEnabled.value
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun togglePinSession(sessionId: String) {
        val updated = _sessions.value.map {
            if (it.id == sessionId) it.copy(pinned = !it.pinned, lastUpdatedAt = System.currentTimeMillis()) else it
        }
        _sessions.value = updated
        viewModelScope.launch(Dispatchers.IO) {
            storageManager.saveSessions(updated)
        }
    }

    fun toggleSessionSelection(sessionId: String) {
        val current = _selectedSessionIds.value.toMutableSet()
        if (current.contains(sessionId)) {
            current.remove(sessionId)
        } else {
            current.add(sessionId)
        }
        _selectedSessionIds.value = current
        if (current.isEmpty()) {
            _isMultiSelectMode.value = false
        } else {
            _isMultiSelectMode.value = true
        }
    }

    fun startMultiSelect(sessionId: String) {
        _isMultiSelectMode.value = true
        _selectedSessionIds.value = setOf(sessionId)
    }

    fun selectAllSessions() {
        _selectedSessionIds.value = _sessions.value.map { it.id }.toSet()
        _isMultiSelectMode.value = true
    }

    fun clearMultiSelect() {
        _selectedSessionIds.value = emptySet()
        _isMultiSelectMode.value = false
    }

    fun deleteSelectedSessions() {
        val idsToDelete = _selectedSessionIds.value
        if (idsToDelete.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = _sessions.value.filterNot { idsToDelete.contains(it.id) }
            _sessions.value = updated
            storageManager.saveSessions(updated)
            clearMultiSelect()
        }
    }

    fun addTagToSession(sessionId: String, tag: String) {
        val updated = _sessions.value.map {
            if (it.id == sessionId && !it.tags.contains(tag)) {
                it.copy(tags = it.tags + tag, lastUpdatedAt = System.currentTimeMillis())
            } else it
        }
        _sessions.value = updated
        viewModelScope.launch(Dispatchers.IO) {
            storageManager.saveSessions(updated)
        }
    }

    fun selectModel(modelName: String) {
        _activeModel.value = modelName
        preferencesRepository.updateDefaultModel(modelName)
    }

    fun loadAvailableModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.fetchAvailableModels(hostUrl)
            result.onSuccess { models ->
                _availableModels.value = models
                if (models.isNotEmpty() && models.none { it.name == _activeModel.value }) {
                    _activeModel.value = models.first().name
                }
            }
        }
    }

    fun startNewChat() {
        val newSession = ChatSessionDto(
            id = java.util.UUID.randomUUID().toString(),
            title = "Chat with ${_activeModel.value}",
            modelName = _activeModel.value,
            createdAt = System.currentTimeMillis(),
            lastUpdatedAt = System.currentTimeMillis(),
            iconName = "ChatBubbleOutline"
        )
        val updatedSessions = listOf(newSession) + _sessions.value
        _sessions.value = updatedSessions
        _currentSessionId.value = newSession.id
        _messages.value = emptyList()
        _uiState.value = ChatUiState.Idle

        viewModelScope.launch(Dispatchers.IO) {
            storageManager.saveSessions(updatedSessions)
        }
    }

    fun startMathSolverChat() {
        startWorkflowChatSession(
            title = "Math Solver Chat",
            iconName = "Functions",
            tags = listOf("Math", "LaTeX"),
            modelName = preferencesRepository.userPreferencesFlow.value.mathModel,
            systemPrompt = """
            You are Celestia's Step-by-Step Math Solver.
            Your job is to solve any math problem with MINIMAL WORDS and STRICT LaTeX math formatting.

            RULES:
            1. OUTPUT ALL equations, formulas, fractions, matrices, derivations, and answers strictly in LaTeX formatting ($...$ for inline, $$...$$ for blocks).
            2. Provide minimal, concise words. Focus entirely on clear step-by-step mathematical steps.
            3. State the Final Answer clearly at the end in a LaTeX block:
               $$ \text{Final Answer: } \mathbf{<answer>} $$
            """.trimIndent()
        )
    }

    fun startCodeRefactorChat() {
        startWorkflowChatSession(
            title = "Code Refactor Chat",
            iconName = "Code",
            tags = listOf("Code", "Refactor"),
            modelName = preferencesRepository.userPreferencesFlow.value.codingModel,
            systemPrompt = "You are an expert Senior Software Engineer. Refactor the provided code snippet to improve readability, performance, type safety, and error handling. Explain your key changes briefly."
        )
    }

    fun startCodeGeneratorChat() {
        startWorkflowChatSession(
            title = "Code Generator Chat",
            iconName = "Terminal",
            tags = listOf("Code", "Generator"),
            modelName = preferencesRepository.userPreferencesFlow.value.codingModel,
            systemPrompt = "You are a lead software architect. Generate clean, efficient, production-ready code for the user's request. Include inline comments, proper error handling, and clean modular structure."
        )
    }

    fun startEmailWriterChat() {
        startWorkflowChatSession(
            title = "Email Writer Chat",
            iconName = "Description",
            tags = listOf("Email", "Productivity"),
            modelName = preferencesRepository.userPreferencesFlow.value.languageModel,
            systemPrompt = "You are a polite executive assistant. Convert bullet points or informal draft notes into a professional, polished email with a subject line."
        )
    }

    fun startGrammarAuditChat() {
        startWorkflowChatSession(
            title = "Grammar Audit Chat",
            iconName = "AutoAwesome",
            tags = listOf("Grammar", "Writing"),
            modelName = preferencesRepository.userPreferencesFlow.value.languageModel,
            systemPrompt = "You are a meticulous copy editor. Correct all grammatical errors, typos, and awkward phrasing in the text, keeping the original voice intact."
        )
    }

    fun startJsonExtractorChat() {
        startWorkflowChatSession(
            title = "JSON Extractor Chat",
            iconName = "Extension",
            tags = listOf("JSON", "Data"),
            modelName = preferencesRepository.userPreferencesFlow.value.codingModel,
            systemPrompt = "Extract key structured entity fields from the text and output pure valid JSON without markdown wrapping or conversational commentary."
        )
    }

    private fun startWorkflowChatSession(
        title: String,
        iconName: String,
        tags: List<String>,
        modelName: String,
        systemPrompt: String
    ) {
        _activeModel.value = modelName
        val newSession = ChatSessionDto(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            modelName = modelName,
            createdAt = System.currentTimeMillis(),
            lastUpdatedAt = System.currentTimeMillis(),
            iconName = iconName,
            tags = tags
        )
        val updatedSessions = listOf(newSession) + _sessions.value
        _sessions.value = updatedSessions
        viewModelScope.launch(Dispatchers.IO) {
            storageManager.saveSessions(updatedSessions)
        }
        _currentSessionId.value = newSession.id

        val systemMsg = UiChatMessage(
            role = "system",
            content = systemPrompt
        )
        val initialMessages = listOf(systemMsg)
        _messages.value = initialMessages
        _uiState.value = ChatUiState.Idle
        viewModelScope.launch(Dispatchers.IO) {
            storageManager.saveMessages(newSession.id, initialMessages)
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        _uiState.value = ChatUiState.Idle
        viewModelScope.launch(Dispatchers.IO) {
            _messages.value = storageManager.loadMessages(sessionId)
        }
    }

    fun closeActiveChat() {
        _currentSessionId.value?.let { sessionId ->
            val msgs = _messages.value
            viewModelScope.launch(Dispatchers.IO) {
                storageManager.saveMessages(sessionId, msgs)
            }
        }
        _currentSessionId.value = null
    }

    fun deleteSession(sessionId: String) {
        CelestiaTaskManager.terminateTask(sessionId)
        val updated = _sessions.value.filter { it.id != sessionId }
        _sessions.value = updated
        if (_currentSessionId.value == sessionId) {
            _currentSessionId.value = null
            _messages.value = emptyList()
            _uiState.value = ChatUiState.Idle
            textBuffer.completeStreaming()
        }
        viewModelScope.launch(Dispatchers.IO) {
            storageManager.deleteSessionData(sessionId)
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val sessionId = _currentSessionId.value ?: run {
            val title = userText.take(30) + if (userText.length > 30) "..." else ""
            val newSession = ChatSessionDto(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                modelName = _activeModel.value,
                createdAt = System.currentTimeMillis(),
                lastUpdatedAt = System.currentTimeMillis(),
                iconName = "ChatBubbleOutline"
            )
            val updatedSessions = listOf(newSession) + _sessions.value
            _sessions.value = updatedSessions
            viewModelScope.launch(Dispatchers.IO) {
                storageManager.saveSessions(updatedSessions)
            }
            _currentSessionId.value = newSession.id
            newSession.id
        }

        val sessionTitle = _sessions.value.firstOrNull { it.id == sessionId }?.title ?: "Chat Conversation"

        val userMsg = UiChatMessage(
            role = "user",
            content = userText
        )

        val updatedList = _messages.value + userMsg
        _messages.value = updatedList

        viewModelScope.launch(Dispatchers.IO) {
            storageManager.saveMessages(sessionId, updatedList)
        }

        if (updatedList.size == 1) {
            val shortTitle = userText.take(30) + if (userText.length > 30) "..." else ""
            val sessionsList = _sessions.value.map {
                if (it.id == sessionId) {
                    it.copy(title = shortTitle, lastUpdatedAt = System.currentTimeMillis())
                } else it
            }
            _sessions.value = sessionsList
            viewModelScope.launch(Dispatchers.IO) {
                storageManager.saveSessions(sessionsList)
            }
        }

        val assistantMsgId = java.util.UUID.randomUUID().toString()
        val initialAssistantMsg = UiChatMessage(
            id = assistantMsgId,
            role = "assistant",
            content = "..."
        )
        _messages.value = updatedList + initialAssistantMsg

        _uiState.value = ChatUiState.WarmingUp(_activeModel.value)

        val streamingJob = viewModelScope.launch(Dispatchers.IO) {
            var tokenCount = 0
            var generationStartTime = 0L

            var finalPayload = updatedList.map {
                OllamaChatMessage(
                    role = it.role,
                    content = it.content,
                    images = it.images
                )
            }

            if (_isWebSearchEnabled.value) {
                val searchResult = webSearchEngine.search(userText)
                if (searchResult.isNotBlank()) {
                    val priorityWebPrompt = """
                    HIGH PRIORITY DIRECTIVE: REAL-TIME WEB SEARCH IS ENABLED.
                    Below are the live, up-to-date web search results fetched specifically for the user's prompt:

                    $searchResult

                    PRIMARY INSTRUCTION FOR YOUR RESPONSE:
                    1. PRIORITIZE AND SUMMARIZE these real-time web search results above all else.
                    2. Synthesize the findings into a clear, concise summary that directly answers the user's query.
                    3. Clearly present the key facts, figures, and details extracted from the search results first.
                    """.trimIndent()

                    val systemContextMsg = OllamaChatMessage(
                        role = "system",
                        content = priorityWebPrompt
                    )
                    finalPayload = listOf(systemContextMsg) + finalPayload
                }
            } else {
                // Autonomous Tool Agent: Automatically calculate math / python expressions
                val currentSession = _sessions.value.firstOrNull { it.id == sessionId }
                val isMathChat = currentSession?.title == "Math Solver Chat" ||
                        userText.contains("factorial", ignoreCase = true) ||
                        userText.contains("sqrt", ignoreCase = true) ||
                        userText.contains("calculate", ignoreCase = true) ||
                        userText.contains("solve", ignoreCase = true) ||
                        userText.contains("math", ignoreCase = true) ||
                        userText.contains("python", ignoreCase = true) ||
                        userText.any { it in "+-*/%^" }

                if (isMathChat) {
                    val pythonCode = ai.moon.celestia.core.util.PythonExecutionEngine.convertPromptToPython(userText)
                    val execRes = ai.moon.celestia.core.util.PythonExecutionEngine.executePythonCode(pythonCode)
                    if (execRes.isSuccess && execRes.stdout.isNotBlank() && execRes.stdout != "Execution finished cleanly.") {
                        val mathToolMsg = OllamaChatMessage(
                            role = "system",
                            content = """
                            ON-DEVICE MATH & PYTHON EXECUTION RESULT:
                            Executed Python Script:
                            ```python
                            $pythonCode
                            ```
                            Exact Output: ${execRes.stdout} (${execRes.executionTimeMs} ms)

                            MANDATE FOR MATH SOLVER RESPONSE:
                            1. Present the executed Python script inside a ```python ``` block.
                            2. Output the exact execution result: ${execRes.stdout}.
                            3. Present the concise step-by-step LaTeX math solution ($...$ for inline, $$...$$ for blocks) arriving at this exact result.
                            """.trimIndent()
                        )
                        finalPayload = listOf(mathToolMsg) + finalPayload
                    }
                }
            }

            val optionsPayload = ai.moon.celestia.core.model.OllamaOptions(
                numGpu = if (numGpuLayers <= 0) 999 else numGpuLayers,
                numCtx = maxContextTokens,
                numThread = numThreads,
                temperature = 0.4f,
                repeatPenalty = 1.15f
            )

            textBuffer.startStreaming()

            repository.streamChat(
                hostUrl = hostUrl,
                model = _activeModel.value,
                messages = finalPayload,
                maxTokens = maxContextTokens,
                options = optionsPayload,
                keepAlive = "10m"
            ).catch { exception ->
                textBuffer.completeStreaming()
                val errorType = if (exception is OllamaHardwareException) {
                    exception.errorType
                } else {
                    OllamaErrorType.Generic(exception.message ?: "Unknown streaming error")
                }
                _uiState.value = ChatUiState.Error(errorType)
            }.collect { chunk ->
                if (_uiState.value is ChatUiState.WarmingUp) {
                    _uiState.value = ChatUiState.Streaming(0f)
                }

                val token = chunk.message?.content.orEmpty()
                if (token.isNotEmpty()) {
                    tokenCount++
                    if (tokenCount == 1) {
                        generationStartTime = System.currentTimeMillis()
                    }
                    textBuffer.appendChunk(token)

                    val elapsedSec = if (generationStartTime > 0L) (System.currentTimeMillis() - generationStartTime) / 1000f else 0f
                    val tokPerSec = if (elapsedSec > 0.05f) tokenCount / elapsedSec else 0f

                    val streamedContent = textBuffer.displayState.value
                    updateAssistantMessageContent(assistantMsgId, streamedContent, tokPerSec)
                    _uiState.value = ChatUiState.Streaming(tokPerSec)
                }

                if (chunk.done) {
                    textBuffer.completeStreaming()

                    val finalTokPerSec = if (chunk.evalCount != null && chunk.evalDuration != null && chunk.evalDuration > 0) {
                        (chunk.evalCount.toDouble() / (chunk.evalDuration.toDouble() / 1_000_000_000.0)).toFloat()
                    } else {
                        val elapsedSec = if (generationStartTime > 0L) (System.currentTimeMillis() - generationStartTime) / 1000f else 0f
                        if (elapsedSec > 0f) tokenCount / elapsedSec else 0f
                    }

                    updateAssistantMessageContent(assistantMsgId, textBuffer.displayState.value, finalTokPerSec)
                    _uiState.value = ChatUiState.Idle

                    val session = _sessions.value.firstOrNull { it.id == sessionId }
                    if (session != null && session.iconName == "ChatBubbleOutline") {
                        val fullContent = _messages.value.joinToString(" ") { it.content }
                        viewModelScope.launch(Dispatchers.IO) {
                            determineIconWithModel(sessionId, fullContent)
                        }
                    }

                    viewModelScope.launch(Dispatchers.IO) {
                        storageManager.saveMessages(sessionId, _messages.value)
                    }
                }
            }
        }

        activeStreamingJob = streamingJob
        CelestiaTaskManager.registerTask(
            id = sessionId,
            title = sessionTitle,
            category = "Chat",
            job = streamingJob,
            onTerminate = {
                textBuffer.completeStreaming()
                _uiState.value = ChatUiState.Idle
                viewModelScope.launch(Dispatchers.IO) {
                    storageManager.saveMessages(sessionId, _messages.value)
                }
            }
        )
    }

    fun stopGeneration() {
        _currentSessionId.value?.let { sessionId ->
            CelestiaTaskManager.terminateTask(sessionId)
        }
        activeStreamingJob?.cancel()
        activeStreamingJob = null
        textBuffer.completeStreaming()
        _uiState.value = ChatUiState.Idle
        _currentSessionId.value?.let { sessionId ->
            viewModelScope.launch(Dispatchers.IO) {
                storageManager.saveMessages(sessionId, _messages.value)
            }
        }
    }

    fun retryLastMessage() {
        val currentMessages = _messages.value
        val lastUserMsg = currentMessages.lastOrNull { it.role == "user" }
        if (lastUserMsg != null) {
            val filtered = currentMessages.filterNot { it.role == "assistant" && it.content == "..." }
            _messages.value = filtered
            sendMessage(lastUserMsg.content)
        }
    }

    private fun updateAssistantMessageContent(id: String, content: String, tokPerSec: Float) {
        val current = _messages.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(
                content = content,
                tokensPerSec = tokPerSec
            )
            _messages.value = current
        }
    }

    private suspend fun determineIconWithModel(sessionId: String, fullContent: String) {
        try {
            val prompt = "Based on this conversation:\n$fullContent\n\nChoose the single most appropriate icon name for this chat from this exact list: [Terminal, Code, Lightbulb, Translate, Description, Search, Star, Rocket, Psychology, AutoAwesome, Book, Build, School, Palette, MusicNote, Shield, Extension, ChatBubbleOutline]. Output ONLY the exact icon name from the list, nothing else."

            var generatedText = ""
            val optionsPayload = ai.moon.celestia.core.model.OllamaOptions(
                numGpu = if (numGpuLayers <= 0) 999 else numGpuLayers,
                numCtx = maxContextTokens,
                numThread = numThreads,
                temperature = 0.4f,
                repeatPenalty = 1.15f
            )
            repository.streamGenerate(
                hostUrl = hostUrl,
                model = _activeModel.value,
                prompt = prompt,
                options = optionsPayload,
                keepAlive = "10m"
            ).collect { chunk ->
                generatedText += chunk.response.orEmpty()
            }

            val trimmed = generatedText.trim().replace(Regex("[^a-zA-Z]"), "")
            val matchedIcon = AVAILABLE_CHAT_ICONS.firstOrNull { trimmed.contains(it, ignoreCase = true) }

            val usedIcons = _sessions.value.filter { it.id != sessionId }.map { it.iconName }.toSet()
            val finalIcon = if (matchedIcon != null && !usedIcons.contains(matchedIcon)) {
                matchedIcon
            } else {
                selectUniqueChatIcon(fullContent, usedIcons)
            }

            val sessionsList = _sessions.value.map {
                if (it.id == sessionId) it.copy(iconName = finalIcon, lastUpdatedAt = System.currentTimeMillis()) else it
            }
            _sessions.value = sessionsList
            viewModelScope.launch(Dispatchers.IO) {
                storageManager.saveSessions(sessionsList)
            }
        } catch (_: Exception) {
            val usedIcons = _sessions.value.filter { it.id != sessionId }.map { it.iconName }.toSet()
            val fallbackIcon = selectUniqueChatIcon(fullContent, usedIcons)
            val sessionsList = _sessions.value.map {
                if (it.id == sessionId) it.copy(iconName = fallbackIcon, lastUpdatedAt = System.currentTimeMillis()) else it
            }
            _sessions.value = sessionsList
            viewModelScope.launch(Dispatchers.IO) {
                storageManager.saveSessions(sessionsList)
            }
        }
    }
}

val AVAILABLE_CHAT_ICONS = listOf(
    "Terminal",
    "Code",
    "Lightbulb",
    "Translate",
    "Description",
    "Search",
    "Star",
    "Rocket",
    "Psychology",
    "AutoAwesome",
    "Book",
    "Build",
    "School",
    "Palette",
    "MusicNote",
    "Shield",
    "Extension",
    "ChatBubbleOutline",
    "Headphones",
    "Pets",
    "DirectionsCar",
    "Fastfood",
    "Monitor",
    "DirectionsRun",
    "FitnessCenter",
    "Flight",
    "CameraAlt",
    "Movie"
)

fun selectUniqueChatIcon(content: String, usedIcons: Set<String>): String {
    val lower = content.lowercase()
    val preferred = when {
        lower.contains("code") || lower.contains("debug") || lower.contains("function") || lower.contains("programming") || lower.contains("kotlin") || lower.contains("java") -> listOf("Code", "Terminal", "Build")
        lower.contains("summarize") || lower.contains("article") || lower.contains("text") || lower.contains("read") -> listOf("Description", "Book", "Search")
        lower.contains("translate") || lower.contains("language") || lower.contains("spanish") || lower.contains("french") -> listOf("Translate", "AutoAwesome")
        lower.contains("explain") || lower.contains("idea") || lower.contains("topic") || lower.contains("why") -> listOf("Lightbulb", "Psychology", "School")
        lower.contains("space") || lower.contains("rocket") || lower.contains("launch") || lower.contains("fast") -> listOf("Rocket", "Star")
        lower.contains("headphone") || lower.contains("audio") || lower.contains("music") || lower.contains("sound") -> listOf("Headphones", "MusicNote")
        lower.contains("horse") || lower.contains("dog") || lower.contains("cat") || lower.contains("animal") || lower.contains("pet") -> listOf("Pets")
        lower.contains("car") || lower.contains("drive") || lower.contains("vehicle") || lower.contains("auto") -> listOf("DirectionsCar")
        lower.contains("food") || lower.contains("eat") || lower.contains("recipe") || lower.contains("cook") -> listOf("Fastfood")
        lower.contains("computer") || lower.contains("monitor") || lower.contains("pc") || lower.contains("screen") -> listOf("Monitor")
        lower.contains("run") || lower.contains("exercise") || lower.contains("workout") || lower.contains("sport") -> listOf("DirectionsRun", "FitnessCenter")
        lower.contains("fly") || lower.contains("flight") || lower.contains("airplane") || lower.contains("travel") -> listOf("Flight")
        lower.contains("photo") || lower.contains("camera") || lower.contains("picture") || lower.contains("image") -> listOf("CameraAlt")
        lower.contains("movie") || lower.contains("film") || lower.contains("video") || lower.contains("watch") -> listOf("Movie")
        else -> emptyList()
    }

    for (icon in preferred) {
        if (!usedIcons.contains(icon)) {
            return icon
        }
    }

    for (icon in AVAILABLE_CHAT_ICONS) {
        if (!usedIcons.contains(icon)) {
            return icon
        }
    }

    return "ChatBubbleOutline"
}