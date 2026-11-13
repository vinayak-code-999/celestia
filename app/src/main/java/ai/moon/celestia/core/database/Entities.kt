package ai.moon.celestia.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chat_sessions",
    indices = [Index(value = ["lastUpdatedAt"]), Index(value = ["pinned"])]
)
data class ChatSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val modelName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val iconName: String = "ChatBubbleOutline",
    val pinned: Boolean = false,
    val tagsJson: String = "[]" // Serialized List<String> of tags
)

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId", "timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val imagesJson: String = "[]", // Serialized List<String> of base64 images
    val timestamp: Long = System.currentTimeMillis(),
    val tokensPerSec: Float = 0f
)

@Entity(tableName = "saved_summaries")
data class SavedSummaryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sourceText: String,
    val summaryContent: String,
    val style: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "translation_history")
data class TranslationHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val tone: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_prompts")
data class CustomPromptEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val systemPrompt: String,
    val templateText: String,
    val category: String,
    val iconName: String = "AutoAwesome",
    val isPreset: Boolean = false
)
