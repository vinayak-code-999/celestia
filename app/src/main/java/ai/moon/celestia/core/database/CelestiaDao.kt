package ai.moon.celestia.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSessionOnce(sessionId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface SummaryDao {
    @Query("SELECT * FROM saved_summaries ORDER BY createdAt DESC")
    fun getAllSummaries(): Flow<List<SavedSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SavedSummaryEntity)

    @Query("DELETE FROM saved_summaries WHERE id = :id")
    suspend fun deleteSummary(id: String)
}

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translation_history ORDER BY createdAt DESC")
    fun getAllTranslations(): Flow<List<TranslationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationHistoryEntity)
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM custom_prompts ORDER BY title ASC")
    fun getAllPrompts(): Flow<List<CustomPromptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: CustomPromptEntity)

    @Query("DELETE FROM custom_prompts WHERE id = :id")
    suspend fun deletePrompt(id: String)
}
