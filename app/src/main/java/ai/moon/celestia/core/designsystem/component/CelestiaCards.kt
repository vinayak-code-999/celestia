package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.core.designsystem.theme.CelestiaThemeStyle
import ai.moon.celestia.core.designsystem.theme.LocalCelestiaThemeDecoration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color? = null,
    shape: Shape? = null,
    content: @Composable () -> Unit
) {
    val decoration = LocalCelestiaThemeDecoration.current
    val defaultShape: Shape = shape ?: when {
        decoration.isCutCorner -> CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp, topEnd = 4.dp, bottomStart = 4.dp)
        decoration.style == CelestiaThemeStyle.NeoBrutalist -> RoundedCornerShape(8.dp)
        decoration.style == CelestiaThemeStyle.SquishyClay -> RoundedCornerShape(28.dp)
        else -> RoundedCornerShape(22.dp)
    }

    val finalBorderColor = borderColor ?: when (decoration.style) {
        CelestiaThemeStyle.NeoBrutalist -> Color.Black
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (decoration.style == CelestiaThemeStyle.NeoBrutalist) 4.dp else 2.dp,
                shape = defaultShape
            ),
        shape = defaultShape,
        color = backgroundColor,
        border = BorderStroke(0.4.dp, finalBorderColor),
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
fun CelestiaCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape? = null,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        shape = shape,
        content = content
    )
}

@Composable
fun CelestiaHardwareErrorChip(
    message: String,
    onSwitchModel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hardware Memory Limit Exceeded",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD32F2F)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            CelestiaButton(
                text = "Switch",
                onClick = onSwitchModel,
                icon = Icons.Default.SwapHoriz,
                containerColor = Color(0xFFD32F2F)
            )
        }
    }
}
