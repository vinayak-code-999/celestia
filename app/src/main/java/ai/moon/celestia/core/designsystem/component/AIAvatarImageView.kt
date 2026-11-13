package ai.moon.celestia.core.designsystem.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView

/**
 * Android View wrapper that renders the 3D Gradient Orb Copilot Avatar,
 * replacing legacy 2D sprite-based anime companion views.
 */
class AIAvatarImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class AvatarState {
        IDLE,           // Gentle bobbing/floating (calm)
        PROCESSING,     // Concentrated thinking pose
        GENERATING,     // Joyful, active waving or wand pose
        EXECUTING,      // Energetic pointing/active pose
        CONNECTED,      // Happy, wide-arms welcoming pose
        DISCONNECTED    // Dizzy/tired, slouching with slow motion
    }

    private val composeView = ComposeView(context)

    private var _currentState: AvatarState = AvatarState.IDLE
    var currentState: AvatarState
        get() = _currentState
        set(value) {
            _currentState = value
            updateComposeOrb()
        }

    init {
        addView(composeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        updateComposeOrb()
    }

    private fun updateComposeOrb() {
        val orbState = when (_currentState) {
            AvatarState.IDLE -> AiState.IDLE
            AvatarState.PROCESSING -> AiState.PROCESSING
            AvatarState.GENERATING -> AiState.TALKING
            AvatarState.EXECUTING -> AiState.EXECUTING
            AvatarState.CONNECTED -> AiState.CONNECTED
            AvatarState.DISCONNECTED -> AiState.DISCONNECTED
        }

        composeView.setContent {
            AiOrbAvatar(state = orbState)
        }
    }
}
