package ai.moon.celestia.feature.settings.components

import ai.moon.celestia.core.designsystem.animation.bounceClickable
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.model.OllamaModelItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategoryDefaultsCard(
    availableModels: List<OllamaModelItem>,
    chatModel: String,
    onChatModelChange: (String) -> Unit,
    mathModel: String,
    onMathModelChange: (String) -> Unit,
    codingModel: String,
    onCodingModelChange: (String) -> Unit,
    languageModel: String,
    onLanguageModelChange: (String) -> Unit,
    ocrModel: String,
    onOcrModelChange: (String) -> Unit,
    chatMaxWords: Int,
    onChatMaxWordsChange: (Int) -> Unit,
    summarizeMaxWords: Int,
    onSummarizeMaxWordsChange: (Int) -> Unit,
    translateMaxWords: Int,
    onTranslateMaxWordsChange: (Int) -> Unit,
    workbenchMaxWords: Int,
    onWorkbenchMaxWordsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CelestiaCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Category Specific Models
            if (availableModels.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Category-Specific Default AI Models",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    CategoryModelSelectorItem(
                        categoryTitle = "💬 General Chat Model",
                        categoryIcon = Icons.Default.Person,
                        selectedModel = chatModel,
                        availableModels = availableModels,
                        onSelectModel = onChatModelChange
                    )

                    CategoryModelSelectorItem(
                        categoryTitle = "📐 Math & Science Model",
                        categoryIcon = Icons.Default.Psychology,
                        selectedModel = mathModel,
                        availableModels = availableModels,
                        onSelectModel = onMathModelChange
                    )

                    CategoryModelSelectorItem(
                        categoryTitle = "💻 Coding & Development Model",
                        categoryIcon = Icons.Default.Memory,
                        selectedModel = codingModel,
                        availableModels = availableModels,
                        onSelectModel = onCodingModelChange
                    )

                    CategoryModelSelectorItem(
                        categoryTitle = "🌐 Language & Translation Model",
                        categoryIcon = Icons.Default.AutoAwesome,
                        selectedModel = languageModel,
                        availableModels = availableModels,
                        onSelectModel = onLanguageModelChange
                    )

                    CategoryModelSelectorItem(
                        categoryTitle = "📷 OCR & Document Model",
                        categoryIcon = Icons.Default.Radar,
                        selectedModel = ocrModel,
                        availableModels = availableModels,
                        onSelectModel = onOcrModelChange
                    )
                }
            }

            // Word Count Sliders
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Max Word Count Limits for Tools",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                WordCountSliderItem(
                    title = "Chat Conversations",
                    value = chatMaxWords,
                    onValueChange = onChatMaxWordsChange,
                    range = 250f..3000f
                )

                WordCountSliderItem(
                    title = "Summarize Tool",
                    value = summarizeMaxWords,
                    onValueChange = onSummarizeMaxWordsChange,
                    range = 100f..2000f
                )

                WordCountSliderItem(
                    title = "Translate Tool",
                    value = translateMaxWords,
                    onValueChange = onTranslateMaxWordsChange,
                    range = 100f..2000f
                )

                WordCountSliderItem(
                    title = "Workbench Tool",
                    value = workbenchMaxWords,
                    onValueChange = onWorkbenchMaxWordsChange,
                    range = 250f..3000f
                )
            }
        }
    }
}

@Composable
fun CategoryModelSelectorItem(
    categoryTitle: String,
    categoryIcon: ImageVector,
    selectedModel: String,
    availableModels: List<OllamaModelItem>,
    onSelectModel: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.bounceClickable { expanded = !expanded }
            ) {
                Text(
                    text = selectedModel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (expanded && availableModels.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(availableModels, key = { it.name }) { model ->
                    CelestiaChip(
                        label = model.name,
                        isSelected = model.name == selectedModel,
                        onSelect = {
                            onSelectModel(model.name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WordCountSliderItem(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "$value words",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = 10
        )
    }
}
