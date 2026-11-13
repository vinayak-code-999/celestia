package ai.moon.celestia.feature.workbench.components

import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaMarkdownView
import ai.moon.celestia.core.designsystem.component.CelestiaMascotAvatar
import ai.moon.celestia.core.designsystem.component.CelestiaSendToActionBar
import ai.moon.celestia.core.designsystem.component.CelestiaTokenSpeedBadge
import ai.moon.celestia.core.designsystem.component.MascotState
import ai.moon.celestia.ui.CelestiaTab
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun WorkbenchOutputCard(
    selectedPresetTitle: String,
    taskOutput: String,
    isExecuting: Boolean,
    tokensPerSec: Float,
    onNavigateToTab: (CelestiaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isExecuting || taskOutput.isNotBlank()) {
        CelestiaCard(modifier = modifier) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedPresetTitle Output",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (taskOutput.isNotBlank()) {
                        CelestiaTokenSpeedBadge(
                            tokensPerSecond = if (tokensPerSec > 0f) tokensPerSec.toDouble() else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isExecuting && taskOutput.isBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        CelestiaMascotAvatar(
                            state = MascotState.Streaming,
                            size = 42.dp,
                            showLabel = false
                        )
                        Text(
                            text = "Running workflow...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    CelestiaMarkdownView(text = taskOutput)

                    if (taskOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CelestiaSendToActionBar(
                            currentToolName = "Workbench",
                            contentToShare = taskOutput,
                            onNavigateToTab = onNavigateToTab
                        )
                    }
                }
            }
        }
    }
}
