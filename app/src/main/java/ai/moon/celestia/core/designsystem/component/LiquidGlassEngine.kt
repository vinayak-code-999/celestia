package ai.moon.celestia.core.designsystem.component

import ai.moon.celestia.ui.LocalGlassPreferences
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tactile Liquid Spring Press Interaction
 */
fun Modifier.liquidPressScale(onClick: () -> Unit): Modifier = this.composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "liquidPressScale"
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

/**
 * Transparent Glassmorphic Effect Modifier featuring surface tint transparency,
 * specular edge highlight, and soft unclipped drop shadow.
 */
fun Modifier.transparentGlassEffect(
    shape: Shape = RoundedCornerShape(50.dp),
    alpha: Float? = null,
    shineIntensity: Float? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 12.dp
): Modifier = this.composed {
    val glassPrefs = LocalGlassPreferences.current
    val effectiveAlpha = alpha ?: glassPrefs.alpha
    val effectiveShine = shineIntensity ?: glassPrefs.shineIntensity

    val onSurface = MaterialTheme.colorScheme.onSurface
    val borderBrush = remember(onSurface, effectiveShine) {
        Brush.verticalGradient(
            colors = listOf(
                onSurface.copy(alpha = effectiveShine),
                onSurface.copy(alpha = effectiveShine * 0.1f)
            )
        )
    }
    val backgroundFill = MaterialTheme.colorScheme.surface.copy(alpha = effectiveAlpha)
    val isDark = isSystemInDarkTheme()

    this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            spotColor = if (isDark) Color.Black.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.60f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
        .clip(shape)
        .background(backgroundFill, shape)
        .border(borderWidth, borderBrush, shape)
}

/**
 * Transparent Glassmorphic Container Composable (`CelestiaTransparentGlassPill`)
 * with soft diffused elevation shadow, transparent background fill, and vertical
 * gradient specular edge highlight.
 */
@Composable
fun CelestiaTransparentGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50.dp),
    alpha: Float? = null,
    shineIntensity: Float? = null,
    elevation: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val glassPrefs = LocalGlassPreferences.current
    val effectiveAlpha = alpha ?: glassPrefs.alpha
    val effectiveShine = shineIntensity ?: glassPrefs.shineIntensity

    Box(
        modifier = modifier
            .transparentGlassEffect(
                shape = shape,
                alpha = effectiveAlpha,
                shineIntensity = effectiveShine,
                elevation = elevation
            )
    ) {
        content()
    }
}

/**
 * Native Liquid Glass Surface Modifier featuring horizontal specular gradient border
 * and ambient casting shadow.
 */
fun Modifier.liquidGlassEffect(
    shape: Shape = RoundedCornerShape(20.dp),
    alpha: Float = 0.82f,
    tintColor: Color? = null,
    elevation: Dp = 12.dp
): Modifier = this.composed {
    val isDark = isSystemInDarkTheme()
    val baseTint = tintColor ?: if (isDark) {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = alpha)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = alpha)
    }

    val glassRotatedBorderBrush = if (isDark) {
        SolidColor(Color.White.copy(alpha = 0.12f))
    } else {
        SolidColor(Color.Black.copy(alpha = 0.10f))
    }

    this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            spotColor = if (isDark) Color.Black.copy(alpha = 0.80f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.50f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
        .clip(shape)
        .background(baseTint, shape)
        .border(0.5.dp, glassRotatedBorderBrush, shape)
}

/**
 * Self-Contained Glass Element Box with top specular reflection shine line
 */
@Composable
fun SelfContainedGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    alpha: Float = 0.82f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlassEffect(
                shape = shape,
                alpha = alpha
            )
    ) {
        content()
    }
}

/**
 * High-Level Reusable Liquid Glass Container Card with top specular reflection shine line
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    alpha: Float = 0.82f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.liquidGlassEffect(
            shape = shape,
            alpha = alpha
        )
    ) {
        // Sharp Foreground Content
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}