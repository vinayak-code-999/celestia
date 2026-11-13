package ai.moon.celestia.core.data

import ai.moon.celestia.core.model.OllamaChatMessage

class OllamaContextManager {

    /**
     * Roughly estimates token count for messages.
     * On average, 1 token ~ 3.5 characters in English text.
     */
    fun estimateTokens(text: String): Int {
        if (text.isEmpty()) return 0
        return (text.length / 3.5).toInt() + 1
    }

    /**
     * Prunes older chat messages if the cumulative token estimate exceeds maxTokens limit.
     * Generates a sliding-window Memory Summary for omitted turns so context is preserved without exceeding TTFT limits.
     */
    fun fitContextWindow(
        messages: List<OllamaChatMessage>,
        maxTokens: Int = 4096
    ): List<OllamaChatMessage> {
        if (messages.isEmpty()) return emptyList()

        val systemMessages = messages.filter { it.role == "system" }
        val conversationMessages = messages.filter { it.role != "system" }

        val systemTokens = systemMessages.sumOf { estimateTokens(it.content) }
        val availableTokens = (maxTokens * 0.85).toInt() - systemTokens // Reserve 15% buffer for response output

        if (availableTokens <= 0) {
            return systemMessages + conversationMessages.takeLast(1)
        }

        val result = mutableListOf<OllamaChatMessage>()
        val droppedMessages = mutableListOf<OllamaChatMessage>()
        var currentTokens = 0

        // Iterate backwards from most recent message
        for (msg in conversationMessages.reversed()) {
            val tokens = estimateTokens(msg.content)
            if (currentTokens + tokens <= availableTokens) {
                result.add(0, msg)
                currentTokens += tokens
            } else {
                droppedMessages.add(0, msg)
            }
        }

        // If older messages were dropped, generate a compact sliding-window memory summary
        if (droppedMessages.isNotEmpty()) {
            val summaryText = droppedMessages.take(6).joinToString("; ") { msg ->
                val shortContent = msg.content.take(60).replace("\n", " ")
                "${msg.role.uppercase()}: $shortContent"
            }
            val memoryMsg = OllamaChatMessage(
                role = "system",
                content = "[CONVERSATION MEMORY SUMMARY OF EARLIER TURNS: $summaryText...]"
            )
            return systemMessages + listOf(memoryMsg) + result
        }

        return systemMessages + result
    }
}