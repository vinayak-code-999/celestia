package ai.moon.celestia.feature.workbench.components

import ai.moon.celestia.core.designsystem.animation.bounceClickable
import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaTextField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun WorkbenchAdvancedParamsCard(
    showAdvancedParams: Boolean,
    onToggleShowAdvancedParams: () -> Unit,
    customSystemPrompt: String,
    onCustomSystemPromptChange: (String) -> Unit,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    topP: Float,
    onTopPChange: (Float) -> Unit,
    repetitionPenalty: Float,
    onRepetitionPenaltyChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    CelestiaCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClickable(onClick = onToggleShowAdvancedParams),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Advanced Parameters & System Prompt",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Icon(
                    imageVector = if (showAdvancedParams) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle advanced parameters",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = showAdvancedParams) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "System Prompt",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    CelestiaTextField(
                        value = customSystemPrompt,
                        onValueChange = onCustomSystemPromptChange,
                        placeholder = "System prompt...",
                        maxLines = 3
                    )

                    Text(
                        text = "Temperature: ${String.format(Locale.US, "%.1f", temperature)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = temperature,
                        onValueChange = onTemperatureChange,
                        valueRange = 0f..2f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "Top P: ${String.format(Locale.US, "%.2f", topP)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = topP,
                        onValueChange = onTopPChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "Repetition Penalty: ${String.format(Locale.US, "%.2f", repetitionPenalty)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Slider(
                        value = repetitionPenalty,
                        onValueChange = onRepetitionPenaltyChange,
                        valueRange = 1f..2f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}
