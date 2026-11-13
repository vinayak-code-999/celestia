package ai.moon.celestia.feature.workbench

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

data class WorkbenchPreset(
    val id: String,
    val title: String,
    val category: String,
    val systemPrompt: String,
    val description: String
)

class WorkbenchViewModel(
    context: Context,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context),
    private val repository: OllamaRepository = OllamaRepository()
) : ViewModel() {

    private val textBuffer = StreamingTextBuffer(viewModelScope, flushIntervalMs = 25L)
    val voiceManager = ai.moon.celestia.core.util.VoiceManager(context)

    val presets = listOf(
        WorkbenchPreset(
            id = "refactor",
            title = "Code Refactor & Audit",
            category = "Development",
            systemPrompt = "You are an expert Senior Software Engineer. Refactor the provided code snippet to improve readability, performance, type safety, and error handling. Explain your key changes briefly.",
            description = "Clean up code snippets according to modern best practices."
        ),
        WorkbenchPreset(
            id = "email",
            title = "Professional Email Writer",
            category = "Productivity",
            systemPrompt = "You are a polite executive assistant. Convert bullet points or informal draft notes into a professional, polished email with a subject line.",
            description = "Draft professional emails from raw bullet points."
        ),
        WorkbenchPreset(
            id = "grammar",
            title = "Grammar & Style Audit",
            category = "Writing",
            systemPrompt = "You are a meticulous copy editor. Correct all grammatical errors, typos, and awkward phrasing in the text, keeping the original voice intact.",
            description = "Fix typos, grammar, and tone instantly."
        ),
        WorkbenchPreset(
            id = "math_solver",
            title = "Math Solver",
            category = "Mathematics",
            systemPrompt = """
            You are Celestia's Step-by-Step Math Solver.
            Your job is to solve the user's math problem with MINIMAL WORDS and STRICT LaTeX math formatting.

            RULES:
            1. OUTPUT ALL equations, formulas, fractions, matrices, derivations, and answers strictly in LaTeX formatting ($...$ for inline, $$...$$ for blocks).
            2. Provide minimal, concise words. Focus entirely on clear step-by-step mathematical steps.
            3. State the Final Answer clearly at the end in a LaTeX block:
               $$ \text{Final Answer: } \mathbf{<answer>} $$
            """.trimIndent(),
            description = "Step-by-step LaTeX math solver with minimal words."
        ),
        WorkbenchPreset(
            id = "code_generator",
            title = "Code Generator",
            category = "Development",
            systemPrompt = "You are a lead software architect. Generate clean, efficient, production-ready code for the user's request. Include inline comments, proper error handling, and clean modular structure.",
            description = "Generate production-ready code in Kotlin, Python, JS, C++, etc."
        ),
        WorkbenchPreset(
            id = "json_extract",
            title = "JSON Data Extractor",
            category = "Data",
            systemPrompt = "Extract key structured entity fields from the text and output pure valid JSON without markdown wrapping or conversational commentary.",
            description = "Extract structured JSON objects from unstructured text."
        )
    )

    private val _selectedPreset = MutableStateFlow(presets.first())
    val selectedPreset: StateFlow<WorkbenchPreset> = _selectedPreset.asStateFlow()

    private val _taskOutput = MutableStateFlow("")
    val taskOutput: StateFlow<String> = _taskOutput.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _topP = MutableStateFlow(0.9f)
    val topP: StateFlow<Float> = _topP.asStateFlow()

    private val _repetitionPenalty = MutableStateFlow(1.1f)
    val repetitionPenalty: StateFlow<Float> = _repetitionPenalty.asStateFlow()

    private val _customSystemPrompt = MutableStateFlow("")
    val customSystemPrompt: StateFlow<String> = _customSystemPrompt.asStateFlow()

    private val _tokensPerSec = MutableStateFlow(0f)
    val tokensPerSec: StateFlow<Float> = _tokensPerSec.asStateFlow()

    private val _availableModels = MutableStateFlow<List<ai.moon.celestia.core.model.OllamaModelItem>>(emptyList())
    val availableModels: StateFlow<List<ai.moon.celestia.core.model.OllamaModelItem>> = _availableModels.asStateFlow()

    private val _selectedModel = MutableStateFlow(preferencesRepository.userPreferencesFlow.value.codingModel)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private var hostUrl = preferencesRepository.userPreferencesFlow.value.hostUrl
    private var activeModel = preferencesRepository.userPreferencesFlow.value.codingModel
    private var maxContextTokens = preferencesRepository.userPreferencesFlow.value.maxContextTokens
    private var autoUnloadModel = preferencesRepository.userPreferencesFlow.value.autoUnloadModel
    private var numThreads = preferencesRepository.userPreferencesFlow.value.numThreads
    private var numGpuLayers = preferencesRepository.userPreferencesFlow.value.numGpuLayers

    init {
        val initialPrefs = preferencesRepository.userPreferencesFlow.value
        hostUrl = initialPrefs.hostUrl
        activeModel = initialPrefs.codingModel
        _selectedModel.value = initialPrefs.codingModel
        maxContextTokens = initialPrefs.maxContextTokens
        autoUnloadModel = initialPrefs.autoUnloadModel
        numThreads = initialPrefs.numThreads
        numGpuLayers = initialPrefs.numGpuLayers
        _customSystemPrompt.value = presets.first().systemPrompt

        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                hostUrl = prefs.hostUrl
                activeModel = prefs.codingModel
                _selectedModel.value = prefs.codingModel
                maxContextTokens = prefs.maxContextTokens
                autoUnloadModel = prefs.autoUnloadModel
                numThreads = prefs.numThreads
                numGpuLayers = prefs.numGpuLayers
            }
        }

        viewModelScope.launch {
            textBuffer.displayState.collect { text ->
                _taskOutput.value = text
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
        preferencesRepository.updateModelForScope(ai.moon.celestia.core.data.AiFunctionScope.Workbench, modelName)
    }

    fun updateHostUrlAndModel(newUrl: String, model: String) {
        hostUrl = newUrl
        activeModel = model
    }

    fun setTemperature(t: Float) { _temperature.value = t }
    fun setTopP(p: Float) { _topP.value = p }
    fun setRepetitionPenalty(rp: Float) { _repetitionPenalty.value = rp }
    fun setCustomSystemPrompt(prompt: String) { _customSystemPrompt.value = prompt }

    fun selectPreset(preset: WorkbenchPreset) {
        _selectedPreset.value = preset
        _customSystemPrompt.value = preset.systemPrompt
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun stopTask() {
        CelestiaTaskManager.terminateTask("workbench")
        textBuffer.completeStreaming()
        _isExecuting.value = false
    }

    fun executeTask(inputContent: String) {
        if (inputContent.isBlank()) return

        _isExecuting.value = true
        _errorMessage.value = null
        _tokensPerSec.value = 0f
        textBuffer.startStreaming()
        
        val options = ai.moon.celestia.core.model.OllamaOptions(
            temperature = _temperature.value,
            topP = _topP.value,
            repeatPenalty = _repetitionPenalty.value,
            numCtx = maxContextTokens,
            numThread = numThreads,
            numGpu = numGpuLayers
        )

        val job = viewModelScope.launch(Dispatchers.IO) {
            var tokenCount = 0
            var generationStartTime = 0L

            repository.streamGenerate(
                hostUrl = hostUrl,
                model = activeModel,
                prompt = inputContent,
                system = _customSystemPrompt.value,
                options = options
            ).catch { e ->
                textBuffer.completeStreaming()
                _isExecuting.value = false
                _errorMessage.value = e.message ?: "Failed to execute workbench task. Verify server connectivity."
            }.collect { chunk ->
                val text = chunk.response.orEmpty()
                if (text.isNotEmpty()) {
                    tokenCount++
                    if (tokenCount == 1) {
                        generationStartTime = System.currentTimeMillis()
                    }
                    textBuffer.appendChunk(text)
                    val elapsedSec = if (generationStartTime > 0L) (System.currentTimeMillis() - generationStartTime) / 1000f else 0f
                    if (elapsedSec > 0.05f) {
                        _tokensPerSec.value = tokenCount / elapsedSec
                    }
                }
                if (chunk.done) {
                    textBuffer.completeStreaming()
                    _isExecuting.value = false
                    if (chunk.evalCount != null && chunk.evalDuration != null && chunk.evalDuration > 0) {
                        _tokensPerSec.value = (chunk.evalCount.toDouble() / (chunk.evalDuration.toDouble() / 1_000_000_000.0)).toFloat()
                    }
                }
            }
        }

        CelestiaTaskManager.registerTask(
            id = "workbench",
            title = _selectedPreset.value.title,
            category = "Workbench",
            job = job,
            onTerminate = {
                textBuffer.completeStreaming()
                _isExecuting.value = false
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.cleanup()
    }
}
