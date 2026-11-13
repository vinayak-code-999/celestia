package ai.moon.celestia.core.network

import ai.moon.celestia.core.model.OllamaChatRequest
import ai.moon.celestia.core.model.OllamaChatResponse
import ai.moon.celestia.core.model.OllamaEmbeddingRequest
import ai.moon.celestia.core.model.OllamaEmbeddingResponse
import ai.moon.celestia.core.model.OllamaErrorType
import ai.moon.celestia.core.model.OllamaGenerateRequest
import ai.moon.celestia.core.model.OllamaGenerateResponse
import ai.moon.celestia.core.model.OllamaTagsResponse
import ai.moon.celestia.core.model.OllamaVersionResponse
import ai.moon.celestia.core.model.ServerHealthStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class OllamaApiService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    suspend fun checkHealth(baseUrl: String): ServerHealthStatus = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trimEnd('/')
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$cleanUrl/api/version")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val versionObj = json.decodeFromString<OllamaVersionResponse>(text)
                ServerHealthStatus.Connected(versionObj.version, cleanUrl)
            } else {
                ServerHealthStatus.Error(OllamaErrorType.Generic("Server status code: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            ServerHealthStatus.Error(OllamaErrorType.ServerUnreachable(cleanUrl))
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun fetchModels(baseUrl: String): Result<OllamaTagsResponse> = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trimEnd('/')
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$cleanUrl/api/tags")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val tags = json.decodeFromString<OllamaTagsResponse>(text)
                Result.success(tags)
            } else {
                Result.failure(Exception("HTTP ${conn.responseCode} fetching models"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    fun streamChat(baseUrl: String, request: OllamaChatRequest): Flow<OllamaChatResponse> = flow<OllamaChatResponse> {
        val cleanUrl = baseUrl.trimEnd('/')
        val url = URL("$cleanUrl/api/chat")
        val jsonPayload = json.encodeToString(OllamaChatRequest.serializer(), request)

        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 60_000
                readTimeout = 180_000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Connection", "Keep-Alive")
                useCaches = false
                setChunkedStreamingMode(0)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val statusCode = conn.responseCode
            if (statusCode != 200) {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorBody = errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $statusCode"
                checkAndThrowOomOrHardwareError(statusCode, errorBody, request.model)
            }

            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            try {
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.isNotBlank()) {
                        try {
                            val chunk = json.decodeFromString<OllamaChatResponse>(line)
                            emit(chunk)
                        } catch (_: Exception) {
                            // Skip malformed chunk
                        }
                    }
                    line = reader.readLine()
                }
            } finally {
                reader.close()
            }
        } finally {
            conn?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    fun streamGenerate(baseUrl: String, request: OllamaGenerateRequest): Flow<OllamaGenerateResponse> = flow<OllamaGenerateResponse> {
        val cleanUrl = baseUrl.trimEnd('/')
        val url = URL("$cleanUrl/api/generate")
        val jsonPayload = json.encodeToString(OllamaGenerateRequest.serializer(), request)

        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 60_000
                readTimeout = 180_000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Connection", "Keep-Alive")
                useCaches = false
                setChunkedStreamingMode(0)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val statusCode = conn.responseCode
            if (statusCode != 200) {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorBody = errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $statusCode"
                checkAndThrowOomOrHardwareError(statusCode, errorBody, request.model)
            }

            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            try {
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.isNotBlank()) {
                        try {
                            val chunk = json.decodeFromString<OllamaGenerateResponse>(line)
                            emit(chunk)
                        } catch (_: Exception) {
                            // Skip malformed chunk
                        }
                    }
                    line = reader.readLine()
                }
            } finally {
                reader.close()
            }
        } finally {
            conn?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun unloadModel(baseUrl: String, modelName: String): Boolean = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trimEnd('/')
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$cleanUrl/api/generate")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            val jsonPayload = """{"model":"$modelName","keep_alive":0}"""
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }
            conn.responseCode == 200
        } catch (_: Exception) {
            false
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun fetchEmbedding(baseUrl: String, model: String, prompt: String): List<Float> = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trimEnd('/')
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$cleanUrl/api/embeddings")
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            val requestObj = OllamaEmbeddingRequest(model = model, prompt = prompt)
            val jsonPayload = json.encodeToString(OllamaEmbeddingRequest.serializer(), requestObj)

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val resObj = json.decodeFromString<OllamaEmbeddingResponse>(text)
                resObj.embedding
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            conn?.disconnect()
        }
    }

    private fun checkAndThrowOomOrHardwareError(statusCode: Int, errorBody: String, modelName: String): Nothing {
        val lowerText = errorBody.lowercase()
        if (statusCode == 500 ||
            lowerText.contains("requires more vram") ||
            lowerText.contains("out of memory") ||
            lowerText.contains("cuda") ||
            lowerText.contains("allocation failed")
        ) {
            throw OllamaHardwareException(
                OllamaErrorType.OutOfMemory(
                    rawMessage = "Host machine ran out of VRAM/RAM trying to load '$modelName'. Try selecting a smaller model like Gemma 2B or Phi-3."
                )
            )
        } else if (statusCode == 404 || lowerText.contains("not found")) {
            throw OllamaHardwareException(
                OllamaErrorType.ModelNotFound(modelName)
            )
        } else {
            throw OllamaHardwareException(
                OllamaErrorType.Generic("Server error ($statusCode): $errorBody")
            )
        }
    }
}

class OllamaHardwareException(val errorType: OllamaErrorType) : Exception(
    when (errorType) {
        is OllamaErrorType.OutOfMemory -> errorType.rawMessage
        is OllamaErrorType.ModelNotFound -> "Model '${errorType.modelName}' not found on host."
        is OllamaErrorType.ServerUnreachable -> "Host server unreachable at ${errorType.hostUrl}."
        is OllamaErrorType.Timeout -> errorType.message
        is OllamaErrorType.Generic -> errorType.message
    }
)
