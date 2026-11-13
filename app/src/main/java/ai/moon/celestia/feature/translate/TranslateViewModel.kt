package ai.moon.celestia.feature.translate

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

class TranslateViewModel(
    context: Context,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context),
    private val repository: OllamaRepository = OllamaRepository()
) : ViewModel() {

    private val textBuffer = StreamingTextBuffer(viewModelScope, flushIntervalMs = 50L)

    private val _sourceLang = MutableStateFlow("English")
    val sourceLang: StateFlow<String> = _sourceLang.asStateFlow()

    private val _targetLang = MutableStateFlow("Spanish")
    val targetLang: StateFlow<String> = _targetLang.asStateFlow()

    private val _selectedTone = MutableStateFlow("Natural")
    val selectedTone: StateFlow<String> = _selectedTone.asStateFlow()

    private val _translationOutput = MutableStateFlow("")
    val translationOutput: StateFlow<String> = _translationOutput.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _tokensPerSec = MutableStateFlow(0.0)
    val tokensPerSec: StateFlow<Double> = _tokensPerSec.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _availableModels = MutableStateFlow<List<ai.moon.celestia.core.model.OllamaModelItem>>(emptyList())
    val availableModels: StateFlow<List<ai.moon.celestia.core.model.OllamaModelItem>> = _availableModels.asStateFlow()

    private val _selectedModel = MutableStateFlow(preferencesRepository.userPreferencesFlow.value.languageModel)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    val availableLanguages = listOf("English", "Spanish", "French", "German", "Japanese", "Chinese", "Hindi", "Arabic")
    val availableTones = listOf("Natural", "Formal", "Casual", "Technical")

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
                _translationOutput.value = text
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
        preferencesRepository.updateModelForScope(ai.moon.celestia.core.data.AiFunctionScope.Translate, modelName)
    }

    fun updateHostUrlAndModel(newUrl: String, model: String) {
        hostUrl = newUrl
        activeModel = model
    }

    fun swapLanguages() {
        val temp = _sourceLang.value
        _sourceLang.value = _targetLang.value
        _targetLang.value = temp
    }

    fun selectSourceLang(lang: String) {
        _sourceLang.value = lang
    }

    fun selectTargetLang(lang: String) {
        _targetLang.value = lang
    }

    fun selectTone(tone: String) {
        _selectedTone.value = tone
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun stopTranslate() {
        CelestiaTaskManager.terminateTask("translate")
        textBuffer.completeStreaming()
        _isTranslating.value = false
    }

    fun translateText(inputText: String) {
        if (inputText.isBlank()) return

        _isTranslating.value = true
        _errorMessage.value = null
        _tokensPerSec.value = 0.0
        textBuffer.startStreaming()

        val prompt = "Translate the following text from ${_sourceLang.value} to ${_targetLang.value} using a ${_selectedTone.value.lowercase()} tone. Output ONLY the translated text without commentary:\n\n$inputText"

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
                _isTranslating.value = false
                _errorMessage.value = e.message ?: "Failed to translate text. Verify server connectivity."
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
                    _isTranslating.value = false
                }
            }
        }

        CelestiaTaskManager.registerTask(
            id = "translate",
            title = "Translate: ${_sourceLang.value} → ${_targetLang.value}",
            category = "Translate",
            job = job,
            onTerminate = {
                textBuffer.completeStreaming()
                _isTranslating.value = false
            }
        )
    }
}
