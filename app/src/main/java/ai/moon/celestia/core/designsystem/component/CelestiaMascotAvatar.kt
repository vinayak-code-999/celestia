package ai.moon.celestia.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MascotState {
    Idle,
    Connected,
    Processing,
    Thinking,
    Generating,
    Executing,
    Disconnected,
    Error,
    Streaming
}

/**
 * 3D Gradient Orb Copilot Avatar wrapper component replacing legacy 2D sprite avatars.
 */
@Composable
fun CelestiaMascotAvatar(
    modifier: Modifier = Modifier,
    state: MascotState = MascotState.Idle,
    size: Dp = 48.dp,
    tokensPerSecond: Double? = null,
    showLabel: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val orbAiState = when (state) {
        MascotState.Idle -> AiState.IDLE
        MascotState.Connected -> AiState.CONNECTED
        MascotState.Processing -> AiState.PROCESSING
        MascotState.Thinking -> AiState.THINKING
        MascotState.Generating -> AiState.GENERATING
        MascotState.Streaming -> AiState.TALKING
        MascotState.Executing -> AiState.EXECUTING
        MascotState.Disconnected -> AiState.DISCONNECTED
        MascotState.Error -> AiState.ERROR
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AiOrbAvatar(
            state = orbAiState,
            modifier = Modifier.size(size)
        )

        if (showLabel || (state == MascotState.Streaming && tokensPerSecond != null)) {
            val labelText = when (state) {
                MascotState.Streaming -> if (tokensPerSecond != null) "%.1f t/s".format(tokensPerSecond) else "Responding..."
                MascotState.Thinking -> "Thinking..."
                MascotState.Error -> "Offline"
                else -> "Celestia"
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(10.dp)),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = labelText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
