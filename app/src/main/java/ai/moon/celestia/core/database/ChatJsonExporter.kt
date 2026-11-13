package ai.moon.celestia.core.database

import ai.moon.celestia.core.model.OllamaChatMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class JsonExportMessage(
    val role: String,
    val content: String,
    val timestamp: Long,
    val tokensPerSec: Float = 0f,
    val images: List<String> = emptyList()
)

@Serializable
data class JsonExportSessionRecord(
    val sessionId: String,
    val title: String,
    val modelName: String,
    val createdAt: Long,
    val lastUpdatedAt: Long,
    val messages: List<JsonExportMessage>
)

class ChatJsonExporter {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun exportSessionToJson(
        session: ChatSessionEntity,
        messages: List<ChatMessageEntity>
    ): String {
        val record = JsonExportSessionRecord(
            sessionId = session.id,
            title = session.title,
            modelName = session.modelName,
            createdAt = session.createdAt,
            lastUpdatedAt = session.lastUpdatedAt,
            messages = messages.map { msg ->
                val imagesList: List<String> = try {
                    if (msg.imagesJson.isNotBlank() && msg.imagesJson != "[]") {
                        json.decodeFromString<List<String>>(msg.imagesJson)
                    } else emptyList<String>()
                } catch (_: Exception) {
                    emptyList<String>()
                }
                JsonExportMessage(
                    role = msg.role,
                    content = msg.content,
                    timestamp = msg.timestamp,
                    tokensPerSec = msg.tokensPerSec,
                    images = imagesList
                )
            }
        )
        return json.encodeToString(record)
    }

    fun parseJsonToSessionRecord(jsonString: String): JsonExportSessionRecord? {
        return try {
            json.decodeFromString<JsonExportSessionRecord>(jsonString)
        } catch (_: Exception) {
            null
        }
    }

    fun convertRecordToOllamaMessages(record: JsonExportSessionRecord): List<OllamaChatMessage> {
        return record.messages.map {
            OllamaChatMessage(
                role = it.role,
                content = it.content,
                images = it.images
            )
        }
    }
}
