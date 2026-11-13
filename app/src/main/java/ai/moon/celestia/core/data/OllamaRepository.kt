package ai.moon.celestia.core.data

import ai.moon.celestia.core.model.OllamaChatMessage
import ai.moon.celestia.core.model.OllamaChatRequest
import ai.moon.celestia.core.model.OllamaChatResponse
import ai.moon.celestia.core.model.OllamaGenerateRequest
import ai.moon.celestia.core.model.OllamaGenerateResponse
import ai.moon.celestia.core.model.OllamaModelItem
import ai.moon.celestia.core.model.ServerHealthStatus
import ai.moon.celestia.core.network.OllamaApiService
import kotlinx.coroutines.flow.Flow

import ai.moon.celestia.core.model.OllamaOptions

class OllamaRepository(
    private val apiService: OllamaApiService = OllamaApiService(),
    private val contextManager: OllamaContextManager = OllamaContextManager()
) {

    suspend fun checkServerHealth(hostUrl: String): ServerHealthStatus {
        return apiService.checkHealth(hostUrl)
    }

    suspend fun fetchAvailableModels(hostUrl: String): Result<List<OllamaModelItem>> {
        return apiService.fetchModels(hostUrl).map { it.models }
    }

    fun streamChat(
        hostUrl: String,
        model: String,
        messages: List<OllamaChatMessage>,
        maxTokens: Int = 2048,
        options: OllamaOptions? = null,
        keepAlive: String? = "10m"
    ): Flow<OllamaChatResponse> {
        val prunedMessages = contextManager.fitContextWindow(messages, maxTokens)
        val request = OllamaChatRequest(
            model = model,
            messages = prunedMessages,
            stream = true,
            options = options,
            keepAlive = keepAlive
        )
        return apiService.streamChat(hostUrl, request)
    }

    fun streamGenerate(
        hostUrl: String,
        model: String,
        prompt: String,
        system: String? = null,
        images: List<String> = emptyList(),
        options: OllamaOptions? = null,
        keepAlive: String? = "10m"
    ): Flow<OllamaGenerateResponse> {
        val request = OllamaGenerateRequest(
            model = model,
            prompt = prompt,
            system = system,
            stream = true,
            images = images,
            options = options,
            keepAlive = keepAlive
        )
        return apiService.streamGenerate(hostUrl, request)
    }

    suspend fun unloadModel(hostUrl: String, modelName: String): Boolean {
        return apiService.unloadModel(hostUrl, modelName)
    }
}
