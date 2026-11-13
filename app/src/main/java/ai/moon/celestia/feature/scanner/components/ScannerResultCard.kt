package ai.moon.celestia.feature.scanner.components

import ai.moon.celestia.core.designsystem.component.CelestiaCard
import ai.moon.celestia.core.designsystem.component.CelestiaMarkdownView
import ai.moon.celestia.core.designsystem.component.CelestiaSendToActionBar
import ai.moon.celestia.core.designsystem.component.CelestiaTokenSpeedBadge
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
fun ScannerResultCard(
    extractedText: String,
    tokensPerSec: Double,
    onNavigateToTab: (CelestiaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    if (extractedText.isBlank()) return

    CelestiaCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extracted Document Content",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                CelestiaTokenSpeedBadge(tokensPerSecond = tokensPerSec)
            }

            CelestiaMarkdownView(text = extractedText)

            Spacer(modifier = Modifier.height(4.dp))

            CelestiaSendToActionBar(
                currentToolName = "Scanner",
                contentToShare = extractedText,
                onNavigateToTab = onNavigateToTab
            )
        }
    }
}
