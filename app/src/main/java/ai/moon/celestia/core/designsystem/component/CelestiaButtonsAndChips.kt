package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.designsystem.theme.CelestiaThemeStyle
import ai.moon.celestia.core.designsystem.theme.LocalCelestiaThemeDecoration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

fun Modifier.applePressScale(onClick: () -> Unit): Modifier = this.composed {
    var isPressed by remember { mutableStateOf(value = false) }
    val scale = animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                },
                onTap = { onClick() }
            )
        }
}

@Composable
fun CelestiaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    val decoration = LocalCelestiaThemeDecoration.current
    val shape: Shape = when {
        decoration.isCutCorner -> CutCornerShape(8.dp)
        decoration.style == CelestiaThemeStyle.NeoBrutalist -> RoundedCornerShape(6.dp)
        else -> RoundedCornerShape(28.dp)
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .applePressScale(onClick = onClick)
            .then(
                if (decoration.style == CelestiaThemeStyle.NeoBrutalist) {
                    Modifier.shadow(3.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                } else Modifier
            ),
        shape = shape,
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.4f),
        border = if (decoration.style == CelestiaThemeStyle.NeoBrutalist) BorderStroke(2.dp, Color.Black) else null,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                )
            )
        }
    }
}

@Composable
fun CelestiaChip(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val decoration = LocalCelestiaThemeDecoration.current
    val shape: Shape = when {
        decoration.isCutCorner -> CutCornerShape(6.dp)
        decoration.style == CelestiaThemeStyle.NeoBrutalist -> RoundedCornerShape(4.dp)
        else -> RoundedCornerShape(20.dp)
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .applePressScale(onClick = onSelect)
            .then(
                if (decoration.style == CelestiaThemeStyle.NeoBrutalist && isSelected) {
                    Modifier.shadow(2.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
                } else Modifier
            ),
        shape = shape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (decoration.style == CelestiaThemeStyle.NeoBrutalist) BorderStroke(1.5.dp, Color.Black) else null,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun CelestiaCopyButton(
    textToCopy: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .applePressScale(onClick = {
                if (textToCopy.isNotBlank()) {
                    clipboardManager.setText(AnnotatedString(textToCopy))
                    copied = true
                }
            }),
        shape = RoundedCornerShape(12.dp),
        color = if (copied) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (copied) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, if (copied) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = "Copy text",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (copied) "Copied!" else "Copy",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CelestiaTokenSpeedBadge(
    tokensPerSecond: Double? = null,
    modifier: Modifier = Modifier
) {
    val displaySpeed = tokensPerSecond ?: 0.0
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Token Speed",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (displaySpeed > 0.0) "%.1f t/s".format(displaySpeed) else "Fast LLM",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
