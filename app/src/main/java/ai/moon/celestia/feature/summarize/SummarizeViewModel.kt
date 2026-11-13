package ai.moon.celestia.feature.summarize

import ai.moon.celestia.core.data.CelestiaTaskManager
import ai.moon.celestia.core.data.OllamaRepository
import ai.moon.celestia.core.datastore.UserPreferencesRepository
import ai.moon.celestia.core.designsystem.util.StreamingTextBuffer
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SummarizeViewModel(
    context: Context,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context),
    private val repository: OllamaRepository = OllamaRepository()
) : ViewModel() {

    private val textBuffer = StreamingTextBuffer(viewModelScope, flushIntervalMs = 50L)

    private val _summaryOutput = MutableStateFlow("")
    val summaryOutput: StateFlow<String> = _summaryOutput.asStateFlow()

    private val _selectedStyle = MutableStateFlow("Executive Brief")
    val selectedStyle: StateFlow<String> = _selectedStyle.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    private val _tokensPerSec = MutableStateFlow(0.0)
    val tokensPerSec: StateFlow<Double> = _tokensPerSec.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _availableModels = MutableStateFlow<List<ai.moon.celestia.core.model.OllamaModelItem>>(emptyList())
    val availableModels: StateFlow<List<ai.moon.celestia.core.model.OllamaModelItem>> = _availableModels.asStateFlow()

    private val _selectedModel = MutableStateFlow(preferencesRepository.userPreferencesFlow.value.languageModel)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    val availableStyles = listOf("Executive Brief", "Bullet Points", "Key Takeaways", "Concise Overview")

    private var hostUrl = preferencesRepository.userPreferencesFlow.value.hostUrl
    private var activeModel = preferencesRepository.userPreferencesFlow.value.languageModel
    private var maxContextTokens = preferencesRepository.userPreferencesFlow.value.maxContextTokens
    private var autoUnloadModel = preferencesRepository.userPreferencesFlow.value.autoUnloadModel
    private var numThreads = preferencesRepository.userPreferencesFlow.value.numThreads
    private var numGpuLayers = preferencesRepository.userPreferencesFlow.value.numGpuLayers

    init {
        val initialPrefs = preferencesRepository.userPreferencesFlow.value
        hostUrl = initialPrefs.hostUrl
        activeModel = initialPrefs.languageModel
        _selectedModel.value = initialPrefs.languageModel
        maxContextTokens = initialPrefs.maxContextTokens
        autoUnloadModel = initialPrefs.autoUnloadModel
        numThreads = initialPrefs.numThreads
        numGpuLayers = initialPrefs.numGpuLayers

        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                hostUrl = prefs.hostUrl
                activeModel = prefs.languageModel
                _selectedModel.value = prefs.languageModel
                maxContextTokens = prefs.maxContextTokens
                autoUnloadModel = prefs.autoUnloadModel
                numThreads = prefs.numThreads
                numGpuLayers = prefs.numGpuLayers
            }
        }

        viewModelScope.launch {
            textBuffer.displayState.collect { text ->
                _summaryOutput.value = text
            }
        }
        loadAvailableModels()
    }

    fun loadAvailableModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.fetchAvailableModels(hostUrl)
            result.onSuccess { models ->
                _availableModels.value = models
            }
        }
    }

    fun selectModel(modelName: String) {
        _selectedModel.value = modelName
        activeModel = modelName
        preferencesRepository.updateModelForScope(ai.moon.celestia.core.data.AiFunctionScope.Summarize, modelName)
    }

    fun updateHostUrlAndModel(newUrl: String, model: String) {
        hostUrl = newUrl
        activeModel = model
    }

    fun selectStyle(style: String) {
        _selectedStyle.value = style
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun stopSummarize() {
        CelestiaTaskManager.terminateTask("summarize")
        textBuffer.completeStreaming()
        _isSummarizing.value = false
    }

    fun generateSummary(inputText: String) {
        if (inputText.isBlank()) return

        _isSummarizing.value = true
        _errorMessage.value = null
        _tokensPerSec.value = 0.0
        textBuffer.startStreaming()

        val prompt = when (_selectedStyle.value) {
            "Bullet Points" -> "Summarize the following text in clear, structured bullet points:\n\n$inputText"
            "Key Takeaways" -> "Extract the top 3-5 core key takeaways from the following text:\n\n$inputText"
            "Concise Overview" -> "Provide a very short, 2-sentence concise summary of the following text:\n\n$inputText"
            else -> "Provide an executive brief summary highlighting main context, findings, and conclusions of the following text:\n\n$inputText"
        }

        val startTime = System.currentTimeMillis()
        var tokenCount = 0

        val job = viewModelScope.launch(Dispatchers.IO) {
            repository.streamGenerate(
                hostUrl = hostUrl,
                model = activeModel,
                prompt = prompt,
                options = null
            ).catch { e ->
                textBuffer.completeStreaming()
                _isSummarizing.value = false
                _errorMessage.value = e.message ?: "Failed to generate summary. Verify server connectivity."
            }.collect { chunk ->
                val text = chunk.response.orEmpty()
                if (text.isNotEmpty()) {
                    textBuffer.appendChunk(text)
                    tokenCount += maxOf(1, text.length / 4)
                    val elapsedSec = maxOf(0.1, (System.currentTimeMillis() - startTime) / 1000.0)
                    _tokensPerSec.value = if (chunk.evalCount != null && chunk.evalDuration != null && chunk.evalDuration > 0) {
                        chunk.evalCount.toDouble() / (chunk.evalDuration.toDouble() / 1_000_000_000.0)
                    } else {
                        tokenCount / elapsedSec
                    }
                }
                if (chunk.done) {
                    textBuffer.completeStreaming()
                    _isSummarizing.value = false
                }
            }
        }

        CelestiaTaskManager.registerTask(
            id = "summarize",
            title = "Summarize: ${_selectedStyle.value}",
            category = "Summarize",
            job = job,
            onTerminate = {
                textBuffer.completeStreaming()
                _isSummarizing.value = false
            }
        )
    }
}
