package ai.moon.celestia.feature.translate.components

import ai.moon.celestia.core.data.AiFunctionScope
import ai.moon.celestia.core.designsystem.animation.bounceClickable
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.component.ModelSelectorChip
import ai.moon.celestia.core.model.OllamaModelItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LanguageSelectorCard(
    selectedModel: String,
    availableModels: List<OllamaModelItem>,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    sourceLang: String,
    targetLang: String,
    availableLanguages: List<String>,
    onSwapLanguages: () -> Unit,
    onSelectTargetLang: (String) -> Unit,
    selectedTone: String,
    availableTones: List<String>,
    onSelectTone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Configuration Card (Model + Language Pipeline)
        CelestiaCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Neural Language Translator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Translate with regional nuance and precise tone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ModelSelectorChip(
                        scope = AiFunctionScope.Translate,
                        selectedModel = selectedModel,
                        availableModels = availableModels,
                        onModelSelected = onModelSelected,
                        onRefreshModels = onRefreshModels
                    )
                }

                // Language Pipeline Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = sourceLang,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .bounceClickable(onClick = onSwapLanguages),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap languages",
                            modifier = Modifier.padding(8.dp).size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = targetLang,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Target Language Selector Chips
                Text(
                    text = "Target Language",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableLanguages, key = { it }) { lang ->
                        CelestiaChip(
                            label = lang,
                            isSelected = targetLang == lang,
                            onSelect = { onSelectTargetLang(lang) }
                        )
                    }
                }
            }
        }

        // Tone Selector
        CelestiaCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Translation Tone",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTones.forEach { tone ->
                        CelestiaChip(
                            label = tone,
                            isSelected = selectedTone == tone,
                            onSelect = { onSelectTone(tone) }
                        )
                    }
                }
            }
        }
    }
}
