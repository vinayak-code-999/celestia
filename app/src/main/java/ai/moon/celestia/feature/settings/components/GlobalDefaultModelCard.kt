package ai.moon.celestia.feature.settings.components

import ai.moon.celestia.core.designsystem.animation.bounceClickable
import ai.moon.celestia.core.designsystem.component.CelestiaButton
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaChip
import ai.moon.celestia.core.designsystem.component.CelestiaTextField
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.core.model.OllamaModelItem
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun GlobalDefaultModelCard(
    defaultModel: String,
    availableModels: List<OllamaModelItem>,
    onSelectDefaultModel: (String) -> Unit,
    onRefreshModels: () -> Unit,
    systemPrompt: String,
    onSystemPromptChange: (String) -> Unit,
    maxContextTokens: Int,
    onMaxContextTokensChange: (Int) -> Unit,
    autoUnloadModel: Boolean,
    onAutoUnloadModelChange: (Boolean) -> Unit,
    numThreads: Int,
    onNumThreadsChange: (Int) -> Unit,
    onPurgeVram: () -> Unit,
    modifier: Modifier = Modifier
) {
    val celestiaHaptics = rememberCelestiaHaptics()

    CelestiaCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Global System Prompt Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Global System Prompt",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Core instructions & persona framing applied across all model interactions:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CelestiaTextField(
                    value = systemPrompt,
                    onValueChange = onSystemPromptChange,
                    placeholder = "Enter global system prompt..."
                )
            }

            // Active Default AI Model Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Global Default AI Model",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Active: $defaultModel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    IconButton(
                        onClick = {
                            celestiaHaptics.performActionPop()
                            onRefreshModels()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh models",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (availableModels.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No models found or not connected to Ollama server.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Test connection above or verify host URL.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableModels.forEach { modelItem ->
                            val isSelected = modelItem.name == defaultModel
                            val surfaceColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClickable(onClick = {
                                        celestiaHaptics.performActionPop()
                                        onSelectDefaultModel(modelItem.name)
                                    }),
                                shape = RoundedCornerShape(16.dp),
                                color = surfaceColor,
                                tonalElevation = if (isSelected) 4.dp else 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = null,
                                            tint = contentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = modelItem.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = contentColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val detailsText = buildString {
                                                modelItem.details?.parameterSize?.let { append("Size: $it  ") }
                                                modelItem.details?.family?.let { append("Family: $it  ") }
                                                modelItem.details?.quantizationLevel?.let { append("Quant: $it") }
                                                if (isEmpty() && modelItem.size != null && modelItem.size > 0) {
                                                    append("${modelItem.size / (1024 * 1024 * 1024)} GB")
                                                }
                                            }
                                            if (detailsText.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = detailsText.trim(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = contentColor.copy(alpha = 0.8f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(
                                                text = "Active",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Context Window Size & Optimization
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Context Window Size (Tokens)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1024, 2048, 4096, 8192).forEach { tokens ->
                        CelestiaChip(
                            label = "$tokens",
                            isSelected = maxContextTokens == tokens,
                            onSelect = {
                                celestiaHaptics.performActionPop()
                                onMaxContextTokensChange(tokens)
                            }
                        )
                    }
                }
                Text(
                    text = "Smaller context sizes (1024/2048) drastically increase token generation speed and reduce VRAM usage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                // Auto-Unload Model Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Unload Model When Idle",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Unloads model after task completion to free VRAM/RAM when idle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = autoUnloadModel,
                        onCheckedChange = { checked ->
                            celestiaHaptics.performActionPop()
                            onAutoUnloadModelChange(checked)
                        }
                    )
                }

                // CPU Threads Selector
                Text(
                    text = "CPU Threads",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4, 8, 12).forEach { threads ->
                        CelestiaChip(
                            label = "$threads Threads",
                            isSelected = numThreads == threads,
                            onSelect = {
                                celestiaHaptics.performActionPop()
                                onNumThreadsChange(threads)
                            }
                        )
                    }
                }
            }

            // GPU VRAM Purge Button
            CelestiaButton(
                text = "Purge GPU VRAM & Free RAM",
                onClick = {
                    celestiaHaptics.performActionPop()
                    onPurgeVram()
                },
                icon = Icons.Default.Memory,
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
