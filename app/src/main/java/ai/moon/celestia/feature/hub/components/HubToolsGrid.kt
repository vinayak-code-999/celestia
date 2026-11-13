package ai.moon.celestia.feature.hub.components

import ai.moon.celestia.core.data.AiFunctionScope
import ai.moon.celestia.core.designsystem.animation.bounceClickable
import ai.moon.celestia.core.designsystem.component.ModelSelectorChip
import ai.moon.celestia.core.model.OllamaModelItem
import ai.moon.celestia.ui.CelestiaTab
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
fun HubToolsGrid(
    chatModel: String,
    ocrModel: String,
    summarizeModel: String,
    translateModel: String,
    workbenchModel: String,
    availableModels: List<OllamaModelItem>,
    onSelectModelForScope: (AiFunctionScope, String) -> Unit,
    onRefreshModels: () -> Unit,
    onNavigateToTab: (CelestiaTab) -> Unit,
    onOpenLiveVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header: Core Assistant
        Text(
            text = "Primary Assistant",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        HubToolRow(
            scope = AiFunctionScope.Chat,
            activeModel = chatModel,
            availableModels = availableModels,
            onSelectModel = { onSelectModelForScope(AiFunctionScope.Chat, it) },
            onRefreshModels = onRefreshModels,
            onClick = { onNavigateToTab(CelestiaTab.Chat) }
        )

        // Section Header: Specialized Modules
        Text(
            text = "Specialized AI Tools & Workspaces",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
        )

        HubToolRow(
            scope = AiFunctionScope.LiveVoice,
            activeModel = chatModel,
            availableModels = availableModels,
            onSelectModel = { onSelectModelForScope(AiFunctionScope.Chat, it) },
            onRefreshModels = onRefreshModels,
            onClick = onOpenLiveVoice
        )

        HubToolRow(
            scope = AiFunctionScope.Scanner,
            activeModel = ocrModel,
            availableModels = availableModels,
            onSelectModel = { onSelectModelForScope(AiFunctionScope.Scanner, it) },
            onRefreshModels = onRefreshModels,
            onClick = { onNavigateToTab(CelestiaTab.Scanner) }
        )

        HubToolRow(
            scope = AiFunctionScope.Summarize,
            activeModel = summarizeModel,
            availableModels = availableModels,
            onSelectModel = { onSelectModelForScope(AiFunctionScope.Summarize, it) },
            onRefreshModels = onRefreshModels,
            onClick = { onNavigateToTab(CelestiaTab.Summarize) }
        )

        HubToolRow(
            scope = AiFunctionScope.Translate,
            activeModel = translateModel,
            availableModels = availableModels,
            onSelectModel = { onSelectModelForScope(AiFunctionScope.Translate, it) },
            onRefreshModels = onRefreshModels,
            onClick = { onNavigateToTab(CelestiaTab.Translate) }
        )

        HubToolRow(
            scope = AiFunctionScope.Workbench,
            activeModel = workbenchModel,
            availableModels = availableModels,
            onSelectModel = { onSelectModelForScope(AiFunctionScope.Workbench, it) },
            onRefreshModels = onRefreshModels,
            onClick = { onNavigateToTab(CelestiaTab.Workbench) }
        )
    }
}

@Composable
fun HubToolRow(
    scope: AiFunctionScope,
    activeModel: String,
    availableModels: List<OllamaModelItem>,
    onSelectModel: (String) -> Unit,
    onRefreshModels: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bounceClickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = scope.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scope.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ModelSelectorChip(
                        scope = scope,
                        selectedModel = activeModel,
                        availableModels = availableModels,
                        onModelSelected = onSelectModel,
                        onRefreshModels = onRefreshModels
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
