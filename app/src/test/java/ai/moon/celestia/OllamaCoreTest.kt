package ai.moon.celestia

import ai.moon.celestia.core.data.OllamaContextManager
import ai.moon.celestia.core.database.ChatJsonExporter
import ai.moon.celestia.core.database.ChatMessageEntity
import ai.moon.celestia.core.database.ChatSessionEntity
import ai.moon.celestia.core.model.OllamaChatMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OllamaCoreTest {

    @Test
    fun testContextWindowPruning() {
        val contextManager = OllamaContextManager()
        val messages = listOf(
            OllamaChatMessage("system", "You are Celestia AI."),
            OllamaChatMessage("user", "Old message 1"),
            OllamaChatMessage("assistant", "Old response 1"),
            OllamaChatMessage("user", "Recent message 2")
        )

        val fitted = contextManager.fitContextWindow(messages, maxTokens = 4096)
        assertEquals(4, fitted.size)
        assertEquals("system", fitted.first().role)
    }

    @Test
    fun testJsonSessionExportAndParse() {
        val exporter = ChatJsonExporter()
        val session = ChatSessionEntity(
            title = "Test Session",
            modelName = "gemma:2b"
        )
        val messages = listOf(
            ChatMessageEntity(
                sessionId = session.id,
                role = "user",
                content = "Hello Ollama!"
            ),
            ChatMessageEntity(
                sessionId = session.id,
                role = "assistant",
                content = "Hello human!"
            )
        )

        val jsonString = exporter.exportSessionToJson(session, messages)
        assertTrue(jsonString.contains("Hello Ollama!"))

        val record = exporter.parseJsonToSessionRecord(jsonString)
        assertNotNull(record)
        assertEquals(2, record?.messages?.size)
        assertEquals("gemma:2b", record?.modelName)
    }
}
