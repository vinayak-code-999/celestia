package ai.moon.celestia.feature.translate.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TranslateOutputCard(
    targetLang: String,
    selectedTone: String,
    translationOutput: String,
    isTranslating: Boolean,
    tokensPerSec: Double,
    onReadAloud: () -> Unit,
    onNavigateToTab: (CelestiaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isTranslating && translationOutput.isBlank()) return

    CelestiaCard(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Translation Result ($targetLang - $selectedTone)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (translationOutput.isNotBlank()) {
                        IconButton(
                            onClick = onReadAloud,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        CelestiaTokenSpeedBadge(tokensPerSecond = tokensPerSec)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isTranslating && translationOutput.isBlank()) {
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
                        text = "Translating text into $targetLang...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                CelestiaMarkdownView(text = translationOutput)

                if (translationOutput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CelestiaSendToActionBar(
                        currentToolName = "Translate",
                        contentToShare = translationOutput,
                        onNavigateToTab = onNavigateToTab
                    )
                }
            }
        }
    }
}
