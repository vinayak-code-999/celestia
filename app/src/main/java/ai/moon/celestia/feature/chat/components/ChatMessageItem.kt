package ai.moon.celestia.feature.chat.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.moon.celestia.core.designsystem.component.CelestiaMarkdownView
import ai.moon.celestia.core.designsystem.component.CelestiaMascotAvatar
import ai.moon.celestia.core.designsystem.component.CelestiaSendToActionBar
import ai.moon.celestia.core.designsystem.component.MascotState
import ai.moon.celestia.core.designsystem.util.rememberCelestiaHaptics
import ai.moon.celestia.feature.chat.UiChatMessage
import ai.moon.celestia.ui.CelestiaTab

@Composable
fun ChatMessageItem(
    message: UiChatMessage,
    modifier: Modifier = Modifier,
    mascotState: MascotState = MascotState.Idle,
    userAvatar: String = "Person",
    onSpeak: (() -> Unit)? = null,
    onNavigateToTab: (CelestiaTab) -> Unit = {}
) {
    val isUser = message.role == "user"
    val isSystem = message.role == "system"

    if (isSystem) {
        // System Context & Tool Payload Card
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "System Tool Context",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "System Context / Tool Output",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                CelestiaMarkdownView(text = message.content)
            }
        }
        return
    }

    var isLifted by remember { mutableStateOf(false) }
    val celestiaHaptics = rememberCelestiaHaptics()

    val liftScale by animateFloatAsState(
        targetValue = if (isLifted) 1.015f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "liftScale"
    )

    val liftElevation by animateDpAsState(
        targetValue = if (isLifted) 10.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "liftElevation"
    )

    if (isUser) {
        val userBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            celestiaHaptics.performActionPop()
                            isLifted = !isLifted
                        }
                    )
                },
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(0.88f)
                    .shadow(elevation = liftElevation, shape = userBubbleShape)
                    .graphicsLayer {
                        scaleX = liftScale
                        scaleY = liftScale
                    },
                shape = userBubbleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    CelestiaMarkdownView(text = message.content)
                }
            }
        }
    } else {
        val aiBubbleShape = RoundedCornerShape(20.dp)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            celestiaHaptics.performActionPop()
                            isLifted = !isLifted
                        }
                    )
                }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = liftElevation, shape = aiBubbleShape)
                    .graphicsLayer {
                        scaleX = liftScale
                        scaleY = liftScale
                    },
                shape = aiBubbleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CelestiaMascotAvatar(
                                state = mascotState,
                                size = 38.dp,
                                showLabel = false
                            )
                            Text(
                                text = "Celestia AI",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (onSpeak != null && message.content.isNotBlank() && message.content != "...") {
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    CelestiaMarkdownView(text = message.content)

                    if (message.content.isNotBlank() && message.content != "...") {
                        Spacer(modifier = Modifier.height(10.dp))
                        CelestiaSendToActionBar(
                            currentToolName = "Chat",
                            contentToShare = message.content,
                            onNavigateToTab = onNavigateToTab
                        )
                    }
                }
            }
        }
    }
}
