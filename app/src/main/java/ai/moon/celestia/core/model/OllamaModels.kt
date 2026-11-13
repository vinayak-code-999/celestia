package ai.moon.celestia.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaModelDetails(
    @SerialName("parent_model") val parentModel: String? = null,
    val format: String? = null,
    val family: String? = null,
    val families: List<String>? = null,
    @SerialName("parameter_size") val parameterSize: String? = null,
    @SerialName("quantization_level") val quantizationLevel: String? = null
)

@Serializable
data class OllamaModelItem(
    val name: String,
    val model: String? = null,
    @SerialName("modified_at") val modifiedAt: String? = null,
    val size: Long? = 0L,
    val digest: String? = null,
    val details: OllamaModelDetails? = null
)

@Serializable
data class OllamaTagsResponse(
    val models: List<OllamaModelItem> = emptyList()
)

@Serializable
data class OllamaOptions(
    @SerialName("num_gpu") val numGpu: Int? = null,
    @SerialName("num_ctx") val numCtx: Int = 2048,
    @SerialName("low_vram") val lowVram: Boolean = false,
    @SerialName("num_thread") val numThread: Int? = null,
    @SerialName("temperature") val temperature: Float = 0.4f,
    @SerialName("top_p") val topP: Float = 0.9f,
    @SerialName("repeat_penalty") val repeatPenalty: Float = 1.15f
)

@Serializable
data class OllamaChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String,
    val images: List<String> = emptyList() // Base64 encoded images for vision models
)

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaChatMessage>,
    val stream: Boolean = true,
    val options: OllamaOptions? = null,
    @SerialName("keep_alive") val keepAlive: String? = null
)

@Serializable
data class OllamaChatResponse(
    val model: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val message: OllamaChatMessage? = null,
    val done: Boolean = false,
    @SerialName("total_duration") val totalDuration: Long? = null,
    @SerialName("load_duration") val loadDuration: Long? = null,
    @SerialName("prompt_eval_count") val promptEvalCount: Int? = null,
    @SerialName("eval_count") val evalCount: Int? = null,
    @SerialName("eval_duration") val evalDuration: Long? = null
)

@Serializable
data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val system: String? = null,
    val stream: Boolean = true,
    val images: List<String> = emptyList(),
    val options: OllamaOptions? = null,
    @SerialName("keep_alive") val keepAlive: String? = null
)

@Serializable
data class OllamaGenerateResponse(
    val model: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val response: String? = null,
    val done: Boolean = false,
    @SerialName("total_duration") val totalDuration: Long? = null,
    @SerialName("eval_count") val evalCount: Int? = null,
    @SerialName("eval_duration") val evalDuration: Long? = null
)

@Serializable
data class OllamaEmbeddingRequest(
    val model: String,
    val prompt: String
)

@Serializable
data class OllamaEmbeddingResponse(
    val embedding: List<Float> = emptyList()
)

@Serializable
data class OllamaVersionResponse(
    val version: String
)

sealed interface ServerHealthStatus {
    data object Disconnected : ServerHealthStatus
    data class Connected(val version: String, val hostUrl: String) : ServerHealthStatus
    data class WarmingUp(val modelName: String) : ServerHealthStatus
    data class Error(val errorType: OllamaErrorType) : ServerHealthStatus
}

sealed interface OllamaErrorType {
    data class OutOfMemory(
        val rawMessage: String,
        val suggestedModels: List<String> = listOf("gemma:2b", "phi3", "qwen:1.5b")
    ) : OllamaErrorType

    data class ModelNotFound(val modelName: String) : OllamaErrorType
    data class ServerUnreachable(val hostUrl: String) : OllamaErrorType
    data class Timeout(val message: String) : OllamaErrorType
    data class Generic(val message: String) : OllamaErrorType
}

data class ContextWindowConfig(
    val maxTokens: Int = 4096,
    val systemPrompt: String = "You are Celestia, an intelligent and helpful AI assistant powered by Ollama."
)
