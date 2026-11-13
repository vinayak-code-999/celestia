package ai.moon.celestia.core.data

import ai.moon.celestia.core.database.CelestiaDatabase
import ai.moon.celestia.core.database.ChatMessageEntity
import ai.moon.celestia.core.database.ChatSessionEntity
import ai.moon.celestia.feature.chat.UiChatMessage
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ChatSessionDto(
    val id: String,
    val title: String,
    val modelName: String,
    val createdAt: Long,
    val lastUpdatedAt: Long,
    val iconName: String = "ChatBubbleOutline",
    val pinned: Boolean = false,
    val tags: List<String> = emptyList()
)

class ChatStorageManager(private val context: Context) {
    private val db = CelestiaDatabase.getInstance(context)
    private val dao = db.chatDao()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadSessions(): List<ChatSessionDto> = withContext(Dispatchers.IO) {
        migrateLegacyJsonIfNeeded()
        val sessionsList = mutableListOf<ChatSessionDto>()
        
        // Read from DB
        val sessionEntities = db.query("SELECT * FROM chat_sessions ORDER BY lastUpdatedAt DESC", null)
        sessionEntities.use { cursor ->
            val idIdx = cursor.getColumnIndex("id")
            val titleIdx = cursor.getColumnIndex("title")
            val modelIdx = cursor.getColumnIndex("modelName")
            val createdIdx = cursor.getColumnIndex("createdAt")
            val updatedIdx = cursor.getColumnIndex("lastUpdatedAt")
            val iconIdx = cursor.getColumnIndex("iconName")
            val pinnedIdx = cursor.getColumnIndex("pinned")
            val tagsIdx = cursor.getColumnIndex("tagsJson")

            while (cursor.moveToNext()) {
                val tagsStr = if (tagsIdx != -1) cursor.getString(tagsIdx) else "[]"
                val tagsList: List<String> = try { json.decodeFromString(tagsStr) } catch (_: Exception) { emptyList() }
                sessionsList.add(
                    ChatSessionDto(
                        id = cursor.getString(idIdx),
                        title = cursor.getString(titleIdx),
                        modelName = cursor.getString(modelIdx),
                        createdAt = cursor.getLong(createdIdx),
                        lastUpdatedAt = cursor.getLong(updatedIdx),
                        iconName = if (iconIdx != -1) cursor.getString(iconIdx) else "ChatBubbleOutline",
                        pinned = if (pinnedIdx != -1) cursor.getInt(pinnedIdx) == 1 else false,
                        tags = tagsList
                    )
                )
            }
        }
        sessionsList
    }

    suspend fun saveSessions(sessions: List<ChatSessionDto>) = withContext(Dispatchers.IO) {
        sessions.forEach { dto ->
            dao.insertSession(
                ChatSessionEntity(
                    id = dto.id,
                    title = dto.title,
                    modelName = dto.modelName,
                    createdAt = dto.createdAt,
                    lastUpdatedAt = dto.lastUpdatedAt,
                    iconName = dto.iconName,
                    pinned = dto.pinned,
                    tagsJson = json.encodeToString(dto.tags)
                )
            )
        }
    }

    suspend fun loadMessages(sessionId: String): List<UiChatMessage> = withContext(Dispatchers.IO) {
        val entities = dao.getMessagesForSessionOnce(sessionId)
        entities.map { entity ->
            val images: List<String> = try { json.decodeFromString(entity.imagesJson) } catch (_: Exception) { emptyList() }
            UiChatMessage(
                id = entity.id,
                role = entity.role,
                content = entity.content,
                timestamp = entity.timestamp,
                tokensPerSec = entity.tokensPerSec,
                images = images
            )
        }
    }

    suspend fun saveMessages(sessionId: String, messages: List<UiChatMessage>) = withContext(Dispatchers.IO) {
        messages.forEach { msg ->
            dao.insertMessage(
                ChatMessageEntity(
                    id = msg.id,
                    sessionId = sessionId,
                    role = msg.role,
                    content = msg.content,
                    imagesJson = json.encodeToString(msg.images),
                    timestamp = msg.timestamp,
                    tokensPerSec = msg.tokensPerSec
                )
            )
        }
    }

    suspend fun deleteSessionData(sessionId: String) = withContext(Dispatchers.IO) {
        dao.deleteMessagesForSession(sessionId)
        dao.deleteSession(sessionId)
    }

    suspend fun searchMessagesForQuery(query: String): Set<String> = withContext(Dispatchers.IO) {
        val result = mutableSetOf<String>()
        if (query.isBlank()) return@withContext result
        
        val cursor = db.query("SELECT DISTINCT sessionId FROM chat_messages WHERE content LIKE ?", arrayOf("%$query%"))
        cursor.use {
            val idx = it.getColumnIndex("sessionId")
            while (it.moveToNext()) {
                if (idx != -1) result.add(it.getString(idx))
            }
        }
        result
    }

    private suspend fun migrateLegacyJsonIfNeeded() {
        val sessionsFile = File(context.filesDir, "chat_sessions.json")
        if (sessionsFile.exists()) {
            try {
                val jsonStr = sessionsFile.readText()
                val legacySessions: List<ChatSessionDto> = json.decodeFromString(jsonStr)
                legacySessions.forEach { dto ->
                    dao.insertSession(
                        ChatSessionEntity(
                            id = dto.id,
                            title = dto.title,
                            modelName = dto.modelName,
                            createdAt = dto.createdAt,
                            lastUpdatedAt = dto.lastUpdatedAt,
                            iconName = dto.iconName,
                            pinned = dto.pinned,
                            tagsJson = json.encodeToString(dto.tags)
                        )
                    )
                    // Load and migrate messages file for session
                    val msgFile = File(context.filesDir, "messages_${dto.id}.json")
                    if (msgFile.exists()) {
                        try {
                            val dtos: List<UiChatMessage> = json.decodeFromString(msgFile.readText())
                            dtos.forEach { msg ->
                                dao.insertMessage(
                                    ChatMessageEntity(
                                        id = msg.id,
                                        sessionId = dto.id,
                                        role = msg.role,
                                        content = msg.content,
                                        imagesJson = json.encodeToString(msg.images),
                                        timestamp = msg.timestamp,
                                        tokensPerSec = msg.tokensPerSec
                                    )
                                )
                            }
                            msgFile.delete()
                        } catch (_: Exception) {}
                    }
                }
                sessionsFile.delete()
            } catch (_: Exception) {}
        }
    }
}