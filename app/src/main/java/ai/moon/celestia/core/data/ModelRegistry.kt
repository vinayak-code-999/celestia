package ai.moon.celestia.core.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

enum class AiFunctionScope(
    val key: String,
    val displayName: String,
    val description: String,
    val icon: ImageVector
) {
    Chat("chat", "Chat & Reasoning", "Conversational AI and step-by-step reasoning", Icons.Default.ChatBubbleOutline),
    Scanner("ocr", "Vision & OCR", "Document layout analysis, tables, and image text extraction", Icons.Default.DocumentScanner),
    Summarize("summarize", "Summarizer", "Executive briefs, bullet points, and key takeaways", Icons.Default.Summarize),
    Translate("translate", "Translator", "Multi-lingual neural translation and tone adaptation", Icons.Default.Translate),
    Workbench("workbench", "AI Workbench", "Code refactoring, email generation, and custom prompts", Icons.Default.AutoAwesome),
    LiveVoice("live_voice", "Ambient Live Voice AI", "Hands-free duplex conversational AI assistant", Icons.Default.Mic);

    companion object {
        fun fromKey(key: String): AiFunctionScope {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: Chat
        }
    }
}
