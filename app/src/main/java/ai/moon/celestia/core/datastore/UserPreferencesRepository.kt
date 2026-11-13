package ai.moon.celestia.core.datastore

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class UserPreferences(
    val hostUrl: String = "http://192.168.1.100:11434",
    val defaultModel: String = "gemma:2b",
    val chatModel: String = "gemma:2b",
    val mathModel: String = "gemma:2b",
    val codingModel: String = "gemma:2b",
    val languageModel: String = "gemma:2b",
    val ocrModel: String = "gemma:2b",
    val maxContextTokens: Int = 2048,
    val systemPrompt: String = "You are Celestia, a direct, concise, and intelligent AI assistant. Always answer the user's question directly with zero conversational filler, preamble, or pleasantries.",
    val isDarkMode: Boolean = true,
    val useDynamicColors: Boolean = true,
    val themeMode: String = "System", // "System", "Dark", "Light"
    val themeStyle: String = "NeoBrutalist", // "NeoBrutalist", "RetroSynthwave", "SquishyClay", "SpaceHud", "ClassicCosmic"
    val userAvatar: String = "Person", // "Person", "AutoAwesome", "Face", "Star", "Psychology", "Rocket"
    val chatMaxWords: Int = 1000,
    val summarizeMaxWords: Int = 500,
    val translateMaxWords: Int = 500,
    val workbenchMaxWords: Int = 1000,
    val autoUnloadModel: Boolean = true,
    val numThreads: Int = 8,
    val numGpuLayers: Int = 999,
    val glassBlur: Float = 16f,
    val glassRefraction: Float = 12f,
    val glassAlpha: Float = 0.20f,
    val glassShineIntensity: Float = 0.45f,
    val glassChromaticAberration: Boolean = true,
    val glassWarmRadiance: Float = 0.25f,
    val glassPreset: String = "PeachWarm", // "PeachWarm", "DarkSmoked", "PillSearch", "FrostedCrystal"
    val savedHostsJson: String = "[\"http://192.168.1.100:11434\", \"http://localhost:11434\", \"http://10.0.2.2:11434\"]",
    val hasCompletedFirstRun: Boolean = false,
    val isHapticsEnabled: Boolean = true
) {
    val savedHostsList: List<String>
        get() = try {
            Json.decodeFromString<List<String>>(savedHostsJson).distinct().filter { it.isNotBlank() }
        } catch (_: Exception) {
            listOf("http://192.168.1.100:11434", "http://localhost:11434", "http://10.0.2.2:11434")
        }
}

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("celestia_prefs", Context.MODE_PRIVATE)

    private val _userPreferencesFlow = MutableStateFlow(readCurrentPreferences())
    val userPreferencesFlow: StateFlow<UserPreferences> = _userPreferencesFlow.asStateFlow()

    private fun readCurrentPreferences(): UserPreferences {
        val defModel = prefs.getString("default_model", "gemma:2b") ?: "gemma:2b"
        return UserPreferences(
            hostUrl = prefs.getString("host_url", "http://192.168.1.100:11434") ?: "http://192.168.1.100:11434",
            defaultModel = defModel,
            chatModel = prefs.getString("chat_model", defModel) ?: defModel,
            mathModel = prefs.getString("math_model", defModel) ?: defModel,
            codingModel = prefs.getString("coding_model", defModel) ?: defModel,
            languageModel = prefs.getString("language_model", defModel) ?: defModel,
            ocrModel = prefs.getString("ocr_model", defModel) ?: defModel,
            maxContextTokens = prefs.getInt("max_context_tokens", 2048),
            systemPrompt = prefs.getString("system_prompt", "You are Celestia, a direct, concise, and intelligent AI assistant. Always answer the user's question directly with zero conversational filler, preamble, or pleasantries.")
                ?: "You are Celestia, a direct, concise, and intelligent AI assistant. Always answer the user's question directly with zero conversational filler, preamble, or pleasantries.",
            isDarkMode = prefs.getBoolean("is_dark_mode", true),
            useDynamicColors = prefs.getBoolean("use_dynamic_colors", true),
            themeMode = prefs.getString("theme_mode", "System") ?: "System",
            themeStyle = prefs.getString("theme_style", "NeoBrutalist") ?: "NeoBrutalist",
            userAvatar = prefs.getString("user_avatar", "Person") ?: "Person",
            chatMaxWords = prefs.getInt("chat_max_words", 1000),
            summarizeMaxWords = prefs.getInt("summarize_max_words", 500),
            translateMaxWords = prefs.getInt("translate_max_words", 500),
            workbenchMaxWords = prefs.getInt("workbench_max_words", 1000),
            autoUnloadModel = prefs.getBoolean("auto_unload_model", true),
            numThreads = prefs.getInt("num_threads", 8),
            numGpuLayers = prefs.getInt("num_gpu_layers", 999),
            glassBlur = prefs.getFloat("glass_blur", 16f),
            glassRefraction = prefs.getFloat("glass_refraction", 12f),
            glassAlpha = prefs.getFloat("glass_alpha", 0.20f),
            glassShineIntensity = prefs.getFloat("glass_shine_intensity", 0.45f),
            glassChromaticAberration = prefs.getBoolean("glass_chromatic", true),
            glassWarmRadiance = prefs.getFloat("glass_radiance", 0.25f),
            glassPreset = prefs.getString("glass_preset", "PeachWarm") ?: "PeachWarm",
            savedHostsJson = prefs.getString("saved_hosts_json", "[\"http://192.168.1.100:11434\", \"http://localhost:11434\", \"http://10.0.2.2:11434\"]")
                ?: "[\"http://192.168.1.100:11434\", \"http://localhost:11434\", \"http://10.0.2.2:11434\"]",
            hasCompletedFirstRun = prefs.getBoolean("has_completed_first_run", false),
            isHapticsEnabled = prefs.getBoolean("is_haptics_enabled", true)
        )
    }

    private fun updatePreferences(block: (UserPreferences) -> UserPreferences) {
        val updated = block(_userPreferencesFlow.value)
        prefs.edit()
            .putString("host_url", updated.hostUrl)
            .putString("default_model", updated.defaultModel)
            .putString("chat_model", updated.chatModel)
            .putString("math_model", updated.mathModel)
            .putString("coding_model", updated.codingModel)
            .putString("language_model", updated.languageModel)
            .putString("ocr_model", updated.ocrModel)
            .putInt("max_context_tokens", updated.maxContextTokens)
            .putString("system_prompt", updated.systemPrompt)
            .putBoolean("is_dark_mode", updated.isDarkMode)
            .putBoolean("use_dynamic_colors", updated.useDynamicColors)
            .putString("theme_mode", updated.themeMode)
            .putString("theme_style", updated.themeStyle)
            .putString("user_avatar", updated.userAvatar)
            .putInt("chat_max_words", updated.chatMaxWords)
            .putInt("summarize_max_words", updated.summarizeMaxWords)
            .putInt("translate_max_words", updated.translateMaxWords)
            .putInt("workbench_max_words", updated.workbenchMaxWords)
            .putBoolean("auto_unload_model", updated.autoUnloadModel)
            .putInt("num_threads", updated.numThreads)
            .putInt("num_gpu_layers", updated.numGpuLayers)
            .putFloat("glass_blur", updated.glassBlur)
            .putFloat("glass_refraction", updated.glassRefraction)
            .putFloat("glass_alpha", updated.glassAlpha)
            .putFloat("glass_shine_intensity", updated.glassShineIntensity)
            .putBoolean("glass_chromatic", updated.glassChromaticAberration)
            .putFloat("glass_radiance", updated.glassWarmRadiance)
            .putString("glass_preset", updated.glassPreset)
            .putString("saved_hosts_json", updated.savedHostsJson)
            .putBoolean("has_completed_first_run", updated.hasCompletedFirstRun)
            .putBoolean("is_haptics_enabled", updated.isHapticsEnabled)
            .apply()
        _userPreferencesFlow.value = updated
    }

    fun completeFirstRun() {
        updatePreferences { it.copy(hasCompletedFirstRun = true) }
    }

    fun updateHostUrl(hostUrl: String) {
        val currentHosts = _userPreferencesFlow.value.savedHostsList.toMutableList()
        val cleanUrl = hostUrl.trim().removeSuffix("/")
        if (cleanUrl.isNotBlank() && !currentHosts.contains(cleanUrl)) {
            currentHosts.add(cleanUrl)
        }
        val updatedJson = Json.encodeToString(currentHosts)
        updatePreferences { it.copy(hostUrl = cleanUrl, savedHostsJson = updatedJson) }
    }

    fun removeSavedHost(url: String) {
        val currentHosts = _userPreferencesFlow.value.savedHostsList.toMutableList()
        currentHosts.remove(url)
        val updatedJson = Json.encodeToString(currentHosts)
        updatePreferences { it.copy(savedHostsJson = updatedJson) }
    }

    fun updateGlassBlur(blur: Float) {
        updatePreferences { it.copy(glassBlur = blur) }
    }

    fun updateGlassRefraction(refraction: Float) {
        updatePreferences { it.copy(glassRefraction = refraction) }
    }

    fun updateGlassAlpha(alpha: Float) {
        updatePreferences { it.copy(glassAlpha = alpha) }
    }

    fun updateGlassShineIntensity(intensity: Float) {
        updatePreferences { it.copy(glassShineIntensity = intensity) }
    }

    fun updateGlassChromaticAberration(enabled: Boolean) {
        updatePreferences { it.copy(glassChromaticAberration = enabled) }
    }

    fun updateGlassWarmRadiance(radiance: Float) {
        updatePreferences { it.copy(glassWarmRadiance = radiance) }
    }

    fun applyGlassPreset(preset: String) {
        when (preset) {
            "PeachWarm" -> updatePreferences {
                it.copy(
                    glassPreset = preset,
                    glassBlur = 22f,
                    glassRefraction = 18f,
                    glassAlpha = 0.78f,
                    glassChromaticAberration = true,
                    glassWarmRadiance = 0.45f
                )
            }
            "DarkSmoked" -> updatePreferences {
                it.copy(
                    glassPreset = preset,
                    glassBlur = 18f,
                    glassRefraction = 14f,
                    glassAlpha = 0.88f,
                    glassChromaticAberration = false,
                    glassWarmRadiance = 0.10f
                )
            }
            "PillSearch" -> updatePreferences {
                it.copy(
                    glassPreset = preset,
                    glassBlur = 14f,
                    glassRefraction = 10f,
                    glassAlpha = 0.85f,
                    glassChromaticAberration = true,
                    glassWarmRadiance = 0.15f
                )
            }
            "FrostedCrystal" -> updatePreferences {
                it.copy(
                    glassPreset = preset,
                    glassBlur = 28f,
                    glassRefraction = 22f,
                    glassAlpha = 0.70f,
                    glassChromaticAberration = true,
                    glassWarmRadiance = 0.30f
                )
            }
        }
    }

    fun updateDefaultModel(model: String) {
        updatePreferences { it.copy(defaultModel = model, chatModel = model) }
    }

    fun updateChatModel(model: String) {
        updatePreferences { it.copy(chatModel = model) }
    }

    fun updateMathModel(model: String) {
        updatePreferences { it.copy(mathModel = model) }
    }

    fun updateCodingModel(model: String) {
        updatePreferences { it.copy(codingModel = model) }
    }

    fun updateLanguageModel(model: String) {
        updatePreferences { it.copy(languageModel = model) }
    }

    fun updateOcrModel(model: String) {
        updatePreferences { it.copy(ocrModel = model) }
    }

    fun updateMaxContextTokens(tokens: Int) {
        updatePreferences { it.copy(maxContextTokens = tokens) }
    }

    fun updateSystemPrompt(prompt: String) {
        updatePreferences { it.copy(systemPrompt = prompt) }
    }

    fun updateDarkMode(isDark: Boolean) {
        updatePreferences { it.copy(isDarkMode = isDark) }
    }

    fun updateDynamicColors(useDynamic: Boolean) {
        updatePreferences { it.copy(useDynamicColors = useDynamic) }
    }

    fun updateThemeMode(mode: String) {
        updatePreferences { it.copy(themeMode = mode) }
    }

    fun updateThemeStyle(style: String) {
        updatePreferences { it.copy(themeStyle = style) }
    }

    fun updateUserAvatar(avatar: String) {
        updatePreferences { it.copy(userAvatar = avatar) }
    }

    fun updateChatMaxWords(words: Int) {
        updatePreferences { it.copy(chatMaxWords = words) }
    }

    fun updateSummarizeMaxWords(words: Int) {
        updatePreferences { it.copy(summarizeMaxWords = words) }
    }

    fun updateTranslateMaxWords(words: Int) {
        updatePreferences { it.copy(translateMaxWords = words) }
    }

    fun updateWorkbenchMaxWords(words: Int) {
        updatePreferences { it.copy(workbenchMaxWords = words) }
    }

    fun updateAutoUnloadModel(autoUnload: Boolean) {
        updatePreferences { it.copy(autoUnloadModel = autoUnload) }
    }

    fun updateNumThreads(threads: Int) {
        updatePreferences { it.copy(numThreads = threads) }
    }

    fun updateNumGpuLayers(layers: Int) {
        updatePreferences { it.copy(numGpuLayers = layers) }
    }

    fun updateModelForScope(scope: ai.moon.celestia.core.data.AiFunctionScope, model: String) {
        when (scope) {
            ai.moon.celestia.core.data.AiFunctionScope.Chat -> updateChatModel(model)
            ai.moon.celestia.core.data.AiFunctionScope.Scanner -> updateOcrModel(model)
            ai.moon.celestia.core.data.AiFunctionScope.Summarize -> updateLanguageModel(model)
            ai.moon.celestia.core.data.AiFunctionScope.Translate -> updateLanguageModel(model)
            ai.moon.celestia.core.data.AiFunctionScope.Workbench -> updateCodingModel(model)
            ai.moon.celestia.core.data.AiFunctionScope.LiveVoice -> updateChatModel(model)
        }
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        updatePreferences { it.copy(isHapticsEnabled = enabled) }
    }
}