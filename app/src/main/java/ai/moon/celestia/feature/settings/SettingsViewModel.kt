package ai.moon.celestia.feature.settings

import ai.moon.celestia.core.data.OllamaRepository
import ai.moon.celestia.core.datastore.UserPreferencesRepository
import ai.moon.celestia.core.model.OllamaModelItem
import ai.moon.celestia.core.model.ServerHealthStatus
import ai.moon.celestia.core.network.DiscoveredServer
import ai.moon.celestia.core.network.OllamaDiscoveryManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    context: Context,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context),
    private val repository: OllamaRepository = OllamaRepository()
) : ViewModel() {

    private val discoveryManager = OllamaDiscoveryManager(context)
    val hfModelManager = ai.moon.celestia.core.data.HuggingFaceModelManager.getInstance()
    val hfDownloadState = hfModelManager.downloadState

    private val _selectedSlmModelId = MutableStateFlow(ai.moon.celestia.core.data.HuggingFaceModelManager.DEFAULT_SMOL_LM.id)
    val selectedSlmModelId: StateFlow<String> = _selectedSlmModelId.asStateFlow()

    val discoveredServers: StateFlow<List<DiscoveredServer>> = discoveryManager.discoveredServers
    val isSearchingMdns: StateFlow<Boolean> = discoveryManager.isSearching

    val userPreferences = preferencesRepository.userPreferencesFlow

    private val _healthStatus = MutableStateFlow<ServerHealthStatus>(ServerHealthStatus.Disconnected)
    val healthStatus: StateFlow<ServerHealthStatus> = _healthStatus.asStateFlow()

    private val _availableModels = MutableStateFlow<List<OllamaModelItem>>(emptyList())
    val availableModels: StateFlow<List<OllamaModelItem>> = _availableModels.asStateFlow()

    fun startMdnsScan() {
        discoveryManager.startDiscovery()
    }

    fun stopMdnsScan() {
        discoveryManager.stopDiscovery()
    }

    fun connectToDiscoveredServer(server: DiscoveredServer) {
        val url = server.httpUrl
        updateHostUrl(url)
    }
    
    fun completeFirstRun() {
        preferencesRepository.completeFirstRun()
    }

    fun updateHostUrl(url: String) {
        preferencesRepository.updateHostUrl(url)
        testConnection(url)
    }

    fun removeSavedHost(url: String) {
        preferencesRepository.removeSavedHost(url)
    }

    fun updateGlassBlur(blur: Float) {
        preferencesRepository.updateGlassBlur(blur)
    }

    fun updateGlassRefraction(refraction: Float) {
        preferencesRepository.updateGlassRefraction(refraction)
    }

    fun updateGlassAlpha(alpha: Float) {
        preferencesRepository.updateGlassAlpha(alpha)
    }

    fun updateGlassShineIntensity(intensity: Float) {
        preferencesRepository.updateGlassShineIntensity(intensity)
    }

    fun updateGlassChromaticAberration(enabled: Boolean) {
        preferencesRepository.updateGlassChromaticAberration(enabled)
    }

    fun updateGlassWarmRadiance(radiance: Float) {
        preferencesRepository.updateGlassWarmRadiance(radiance)
    }

    fun applyGlassPreset(preset: String) {
        preferencesRepository.applyGlassPreset(preset)
    }

    fun updateDefaultModel(model: String) {
        preferencesRepository.updateDefaultModel(model)
    }

    fun updateChatModel(model: String) {
        preferencesRepository.updateChatModel(model)
    }

    fun updateMathModel(model: String) {
        preferencesRepository.updateMathModel(model)
    }

    fun updateCodingModel(model: String) {
        preferencesRepository.updateCodingModel(model)
    }

    fun updateLanguageModel(model: String) {
        preferencesRepository.updateLanguageModel(model)
    }

    fun updateOcrModel(model: String) {
        preferencesRepository.updateOcrModel(model)
    }

    fun updateModelForScope(scope: ai.moon.celestia.core.data.AiFunctionScope, model: String) {
        preferencesRepository.updateModelForScope(scope, model)
    }

    fun updateMaxContextTokens(tokens: Int) {
        preferencesRepository.updateMaxContextTokens(tokens)
    }

    fun updateSystemPrompt(prompt: String) {
        preferencesRepository.updateSystemPrompt(prompt)
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        preferencesRepository.updateHapticsEnabled(enabled)
    }

    fun updateDarkMode(isDark: Boolean) {
        preferencesRepository.updateDarkMode(isDark)
    }

    fun updateThemeMode(mode: String) {
        preferencesRepository.updateThemeMode(mode)
    }

    fun updateThemeStyle(style: String) {
        preferencesRepository.updateThemeStyle(style)
    }

    fun updateUserAvatar(avatar: String) {
        preferencesRepository.updateUserAvatar(avatar)
    }

    fun updateChatMaxWords(words: Int) {
        preferencesRepository.updateChatMaxWords(words)
    }

    fun updateSummarizeMaxWords(words: Int) {
        preferencesRepository.updateSummarizeMaxWords(words)
    }

    fun updateTranslateMaxWords(words: Int) {
        preferencesRepository.updateTranslateMaxWords(words)
    }

    fun updateWorkbenchMaxWords(words: Int) {
        preferencesRepository.updateWorkbenchMaxWords(words)
    }

    fun updateAutoUnloadModel(autoUnload: Boolean) {
        preferencesRepository.updateAutoUnloadModel(autoUnload)
    }

    fun updateNumThreads(threads: Int) {
        preferencesRepository.updateNumThreads(threads)
    }

    fun updateNumGpuLayers(layers: Int) {
        preferencesRepository.updateNumGpuLayers(layers)
    }

    fun testConnection(hostUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val health = repository.checkServerHealth(hostUrl)
            _healthStatus.value = health
            if (health is ServerHealthStatus.Connected) {
                val modelsResult = repository.fetchAvailableModels(hostUrl)
                modelsResult.onSuccess { models ->
                    _availableModels.value = models
                }
            }
        }
    }

    fun downloadHfModel(context: Context, modelInfo: ai.moon.celestia.core.data.HuggingFaceModelInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            hfModelManager.downloadModel(context.applicationContext, modelInfo).collect { }
        }
    }

    fun downloadCustomHfUrl(context: Context, url: String) {
        val customModel = ai.moon.celestia.core.data.HuggingFaceModelInfo(
            id = "custom_${System.currentTimeMillis()}",
            name = "Custom Hugging Face Model",
            repoId = "custom/hf-model",
            filename = url.substringAfterLast("/").ifEmpty { "custom-model.gguf" },
            downloadUrl = url
        )
        downloadHfModel(context, customModel)
    }

    fun deleteHfModel(context: Context, modelInfo: ai.moon.celestia.core.data.HuggingFaceModelInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = hfModelManager.getLocalModelFile(context.applicationContext, modelInfo)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    fun selectSlmModel(modelId: String) {
        _selectedSlmModelId.value = modelId
    }

    override fun onCleared() {
        super.onCleared()
        discoveryManager.stopDiscovery()
    }
}
