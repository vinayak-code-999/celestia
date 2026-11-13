package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.designsystem.theme.CelestiaThemeStyle
import ai.moon.celestia.core.designsystem.theme.LocalCelestiaThemeDecoration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CelestiaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSend: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onAttachImage: (() -> Unit)? = null,
    onVoiceInput: (() -> Unit)? = null,
    isListening: Boolean = false,
    isStreaming: Boolean = false,
    maxLines: Int = 5
) {
    val decoration = LocalCelestiaThemeDecoration.current
    val shape: Shape = when {
        decoration.isCutCorner -> CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
        decoration.style == CelestiaThemeStyle.NeoBrutalist -> RoundedCornerShape(12.dp)
        else -> RoundedCornerShape(26.dp)
    }

    val isDark = isSystemInDarkTheme()
    val borderBrush = remember(decoration.style, isDark) {
        if (decoration.style == CelestiaThemeStyle.NeoBrutalist) {
            SolidColor(Color.Black)
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.25f else 0.60f),
                    Color.White.copy(alpha = if (isDark) 0.05f else 0.15f)
                )
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (decoration.style == CelestiaThemeStyle.NeoBrutalist) {
                    Modifier.shadow(3.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                } else {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                    )
                }
            ),
        shape = shape,
        color = if (decoration.style == CelestiaThemeStyle.NeoBrutalist) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f)
        },
        border = BorderStroke(
            if (decoration.style == CelestiaThemeStyle.NeoBrutalist) 2.dp else 1.dp,
            borderBrush
        ),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            // Top Row: Text Input Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    ),
                    maxLines = maxLines,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bottom Control Row: Action buttons (Attach / Voice on left, Send / Stop on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onAttachImage != null) {
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .applePressScale(onClick = onAttachImage),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Attach image",
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    if (onVoiceInput != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isListening) {
                                AudioWaveformVisualizer()
                            }

                            Box(contentAlignment = Alignment.Center) {
                                if (isListening) {
                                    val infinitePulse = rememberInfiniteTransition(label = "micPulse")
                                    val auraScale by infinitePulse.animateFloat(
                                        initialValue = 1.0f,
                                        targetValue = 1.5f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "auraScale"
                                    )
                                    val auraAlpha by infinitePulse.animateFloat(
                                        initialValue = 0.5f,
                                        targetValue = 0.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "auraAlpha"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .graphicsLayer {
                                                scaleX = auraScale
                                                scaleY = auraScale
                                                alpha = auraAlpha
                                            }
                                            .background(MaterialTheme.colorScheme.error, CircleShape)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .applePressScale(onClick = { onVoiceInput() }),
                                    color = if (isListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    contentColor = if (isListening) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice Input",
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Send / Stop button
                if (isStreaming && onStop != null) {
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .applePressScale(onClick = onStop),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop generation",
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                } else if (onSend != null) {
                    val canSend = value.isNotBlank() && !isStreaming
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .applePressScale(onClick = {
                                if (canSend) {
                                    onSend()
                                }
                            }),
                        color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
