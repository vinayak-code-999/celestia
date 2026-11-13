package ai.moon.celestia.feature.translate.components

import ai.moon.celestia.core.designsystem.component.CelestiaButton
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.component.CelestiaTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TranslateInputCard(
    sourceLang: String,
    targetLang: String,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isListening: Boolean,
    onToggleListening: () -> Unit,
    onClearInput: () -> Unit,
    isTranslating: Boolean,
    onTranslate: () -> Unit,
    showQuickPrompts: Boolean,
    onSelectQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Starter Prompts
        if (showQuickPrompts) {
            CelestiaCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "⚡ Quick Translation Prompts",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    val starters = listOf(
                        "💼 Business Email",
                        "💬 Casual Chat",
                        "✈️ Travel Phrases",
                        "📄 Document Abstract"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(starters, key = { it }) { prompt ->
                            CelestiaChip(
                                label = prompt,
                                isSelected = false,
                                onSelect = {
                                    onSelectQuickPrompt(prompt)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Source Text Input Card
        CelestiaCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Text in $sourceLang",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onToggleListening,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Dictate Speech",
                                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = onClearInput,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (isListening) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "🎙️ On-Device Dictating... Speak into microphone",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                CelestiaTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = "Enter text to translate to $targetLang...",
                    maxLines = 5
                )
            }
        }

        // Translate Button
        CelestiaButton(
            text = if (isTranslating) "Translating Text..." else "Translate to $targetLang",
            onClick = onTranslate,
            icon = Icons.Default.Translate,
            enabled = inputText.isNotBlank() && !isTranslating,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
