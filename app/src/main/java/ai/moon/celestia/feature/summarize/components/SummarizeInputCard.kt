package ai.moon.celestia.feature.summarize.components

import ai.moon.celestia.core.data.AiFunctionScope
import ai.moon.celestia.core.designsystem.component.CelestiaButton
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.component.CelestiaTextField
import ai.moon.celestia.core.designsystem.component.ModelSelectorChip
import ai.moon.celestia.core.model.OllamaModelItem
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SummarizeInputCard(
    selectedModel: String,
    availableModels: List<OllamaModelItem>,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    selectedStyle: String,
    availableStyles: List<String>,
    onSelectStyle: (String) -> Unit,
    sourceText: String,
    onSourceTextChange: (String) -> Unit,
    isSummarizing: Boolean,
    showQuickStarters: Boolean,
    onSelectStarterPrompt: (String) -> Unit,
    onSummarize: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Configuration Card (Model + Summary Style Toolbar)
        CelestiaCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Executive Summarizer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Distill long text into structured briefings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ModelSelectorChip(
                        scope = AiFunctionScope.Summarize,
                        selectedModel = selectedModel,
                        availableModels = availableModels,
                        onModelSelected = onModelSelected,
                        onRefreshModels = onRefreshModels
                    )
                }

                // Style Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableStyles, key = { it }) { style ->
                        CelestiaChip(
                            label = style,
                            isSelected = selectedStyle == style,
                            onSelect = { onSelectStyle(style) }
                        )
                    }
                }
            }
        }

        // Quick Starter Prompts
        if (showQuickStarters) {
            CelestiaCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "⚡ Quick Starter Prompts",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    val starters = listOf(
                        "✨ Summarize a news article",
                        "💡 Key takeaways & bullets",
                        "📄 Executive briefing",
                        "📝 Meeting notes recap"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(starters, key = { it }) { prompt ->
                            CelestiaChip(
                                label = prompt,
                                isSelected = false,
                                onSelect = {
                                    onSelectStarterPrompt(prompt)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Input Content Card
        CelestiaCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Source Text Content",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (sourceText.isNotEmpty()) {
                        IconButton(
                            onClick = { onSourceTextChange("") },
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
                Spacer(modifier = Modifier.height(8.dp))
                CelestiaTextField(
                    value = sourceText,
                    onValueChange = onSourceTextChange,
                    placeholder = "Paste article, notes, URL, or long text to summarize...",
                    maxLines = 6
                )
            }
        }

        // Action Button
        CelestiaButton(
            text = if (isSummarizing) "Summarizing Content..." else "Generate $selectedStyle",
            onClick = onSummarize,
            icon = Icons.Default.AutoAwesome,
            enabled = sourceText.isNotBlank() && !isSummarizing,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
