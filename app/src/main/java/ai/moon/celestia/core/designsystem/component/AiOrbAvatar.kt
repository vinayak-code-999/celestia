package ai.moon.celestia.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class AiState {
    IDLE,
    CONNECTED,
    LISTENING,
    PROCESSING,
    THINKING,
    SEARCHING_WEB,
    GENERATING,
    TALKING,
    EXECUTING,
    DISCONNECTED,
    ERROR
}

/**
 * A crystal glass 3D AI Companion Orb with emissive 3D glowing LED eyes,
 * a dynamic talking mouth that opens with voice speech, and elevated floor glow.
 */
@Composable
fun AiOrbAvatar(
    state: AiState,
    modifier: Modifier = Modifier,
    audioLevel: Float = 0f
) {
    // 1. State Color Palettes
    val (targetPrimary, targetSecondary, targetDeep, targetAccent) = when (state) {
        AiState.IDLE -> StateColors(
            primary = Color(0xFF818CF8),    // Soft Periwinkle
            secondary = Color(0xFF4338CA),  // Deep Indigo
            deep = Color(0xFF0F172A),       // Deep Space Obsidian
            accent = Color(0xFFC084FC)      // Lavender Glow
        )
        AiState.CONNECTED -> StateColors(
            primary = Color(0xFF00F2FE),    // Electric Cyan
            secondary = Color(0xFF0284C7),  // Deep Teal
            deep = Color(0xFF032B45),       // Abyss Teal
            accent = Color(0xFF38BDF8)      // Aqua Glow
        )
        AiState.LISTENING -> StateColors(
            primary = Color(0xFF00F2FE),    // Receptive Cyan
            secondary = Color(0xFF0D9488),  // Deep Teal Green
            deep = Color(0xFF022C22),       // Abyss Teal
            accent = Color(0xFF34D399)      // Bright Mint Spark
        )
        AiState.PROCESSING -> StateColors(
            primary = Color(0xFFFFB703),    // Solar Gold Compute
            secondary = Color(0xFFFB8500),  // Amber Orange
            deep = Color(0xFF2A1400),       // Deep Bronze
            accent = Color(0xFFFEF08A)      // Warm Yellow Glow
        )
        AiState.THINKING -> StateColors(
            primary = Color(0xFFC084FC),    // Cosmic Purple
            secondary = Color(0xFF7E22CE),  // Deep Violet Nebula
            deep = Color(0xFF1E0A38),       // Deep Cosmic Core
            accent = Color(0xFFF472B6)      // Pink Quartz Glow
        )
        AiState.SEARCHING_WEB -> StateColors(
            primary = Color(0xFF34D399),    // Emerald Mint
            secondary = Color(0xFF059669),  // Deep Forest
            deep = Color(0xFF022C22),       // Abyss Emerald
            accent = Color(0xFF6EE7B7)      // Quantum Cyan Glow
        )
        AiState.GENERATING -> StateColors(
            primary = Color(0xFF10B981),    // Creation Emerald
            secondary = Color(0xFF059669),  // Deep Mint
            deep = Color(0xFF022016),       // Deep Cyber Core
            accent = Color(0xFFA7F3D0)      // Fresh Mint Burst
        )
        AiState.TALKING -> StateColors(
            primary = Color(0xFFFF2E93),    // Sunset Voice Pink
            secondary = Color(0xFFEC4899),  // Hot Magenta
            deep = Color(0xFF250012),       // Deep Magenta Abyss
            accent = Color(0xFFFDE047)      // Coral Gold Spark
        )
        AiState.EXECUTING -> StateColors(
            primary = Color(0xFF06B6D4),    // Cyber Cyan
            secondary = Color(0xFF0284C7),  // Hyper Blue
            deep = Color(0xFF022016),       // Cyber Black Core
            accent = Color(0xFF67E8F9)      // Cyan Flare
        )
        AiState.DISCONNECTED -> StateColors(
            primary = Color(0xFF94A3B8),    // Titanium Silver
            secondary = Color(0xFF475569),  // Slate Shadow
            deep = Color(0xFF0F172A),       // Cold Graphite Deep
            accent = Color(0xFFE2E8F0)      // Cool Metallic Highlight
        )
        AiState.ERROR -> StateColors(
            primary = Color(0xFFEF4444),    // Crimson Red
            secondary = Color(0xFFDC2626),  // Deep Blood Red
            deep = Color(0xFF2F0404),       // Dark Crimson Core
            accent = Color(0xFFFDBA74)      // Ember Flame Spark
        )
    }

    val colorTween = tween<Color>(450, easing = FastOutSlowInEasing)

    val corePrimary by animateColorAsState(targetPrimary, colorTween, label = "corePrimary")
    val coreSecondary by animateColorAsState(targetSecondary, colorTween, label = "coreSecondary")
    val coreDeep by animateColorAsState(targetDeep, colorTween, label = "coreDeep")
    val coreAccent by animateColorAsState(targetAccent, colorTween, label = "coreAccent")

    // 2. Butter-Smooth Interpolated State Morph Targets
    val targetEyeWScale = when (state) {
        AiState.PROCESSING -> 0.28f   // Inquisitive focused narrowed eyes
        AiState.LISTENING -> 0.24f
        AiState.TALKING -> 0.24f
        AiState.DISCONNECTED -> 0.22f
        else -> 0.22f
    }

    val targetEyeHScale = when (state) {
        AiState.DISCONNECTED -> 0.08f
        AiState.PROCESSING -> 0.24f
        AiState.LISTENING -> 0.38f + (audioLevel.coerceIn(0f, 1f) * 0.10f)
        AiState.TALKING -> 0.36f + (audioLevel.coerceIn(0f, 1f) * 0.12f)
        AiState.GENERATING -> 0.34f
        else -> 0.36f
    }

    val targetOffsetXScale = when (state) {
        AiState.THINKING -> 0.08f
        else -> 0.0f
    }

    val targetOffsetYScale = when (state) {
        AiState.THINKING -> -0.08f
        AiState.LISTENING -> -0.02f
        AiState.DISCONNECTED -> 0.04f
        else -> 0.0f
    }

    val targetEyeRot = when (state) {
        AiState.THINKING -> 6f
        AiState.EXECUTING -> -12f
        AiState.ERROR -> 45f
        else -> 0f
    }

    val targetSmileCurve = when (state) {
        AiState.CONNECTED -> 0.7f
        AiState.TALKING -> 0.35f
        AiState.GENERATING -> 0.25f
        else -> 0.0f
    }

    val targetEyeAlpha = when (state) {
        AiState.DISCONNECTED -> 0.6f
        else -> 1.0f
    }

    val targetMistAlpha = when (state) {
        AiState.DISCONNECTED -> 0.25f
        AiState.IDLE -> 0.45f
        AiState.TALKING, AiState.GENERATING -> 0.85f
        AiState.LISTENING -> 0.75f
        else -> 0.65f
    }

    val stateTween = tween<Float>(450, easing = FastOutSlowInEasing)

    val animEyeWScale by animateFloatAsState(targetEyeWScale, stateTween, label = "animEyeWScale")
    val animEyeHScale by animateFloatAsState(targetEyeHScale, stateTween, label = "animEyeHScale")
    val animOffsetXScale by animateFloatAsState(targetOffsetXScale, stateTween, label = "animOffsetXScale")
    val animOffsetYScale by animateFloatAsState(targetOffsetYScale, stateTween, label = "animOffsetYScale")
    val animEyeRot by animateFloatAsState(targetEyeRot, stateTween, label = "animEyeRot")
    val animSmileCurve by animateFloatAsState(targetSmileCurve, stateTween, label = "animSmileCurve")
    val animEyeAlpha by animateFloatAsState(targetEyeAlpha, stateTween, label = "animEyeAlpha")
    val animMistAlpha by animateFloatAsState(targetMistAlpha, stateTween, label = "animMistAlpha")

    // Continuous Ultra-Smooth Seamless 3D Motion Generators (Zero Jitter)
    val infiniteTransition = rememberInfiniteTransition(label = "render3DOrb")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "phase"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "wavePhase"
    )

    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "particlePhase"
    )

    val stateWaveSpeed = when (state) {
        AiState.PROCESSING -> 2.2f
        AiState.THINKING -> 2.0f
        AiState.TALKING, AiState.GENERATING, AiState.EXECUTING -> 3.0f
        AiState.LISTENING -> 1.5f
        AiState.SEARCHING_WEB -> 2.0f
        AiState.CONNECTED -> 1.0f
        AiState.ERROR -> 4.0f
        else -> 1.0f
    }

    val particles = remember {
        List(12) { index ->
            ParticleSeed(
                angle = (index * (360f / 12f)),
                distFactor = 0.55f + (index % 4) * 0.18f,
                sizeDp = 1.4f + (index % 3) * 0.8f,
                speedFactor = 0.5f + (index % 3) * 0.4f,
                alpha = 0.20f + (index % 4) * 0.10f
            )
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val minDim = size.minDimension

        // Error state smooth tremor
        val jitterX = if (state == AiState.ERROR) sin((phase * 12f).toDouble()).toFloat() * (minDim * 0.012f) else 0f
        val jitterY = if (state == AiState.ERROR) cos((phase * 12f).toDouble()).toFloat() * (minDim * 0.012f) else 0f

        // Seamless Floating Y-bounce (3D Elevation breathing motion)
        val floatY = sin(phase.toDouble()).toFloat() * (minDim * 0.025f) + jitterY
        val cx = width / 2f + jitterX
        val cy = height * 0.46f + floatY
        val radius = minDim * 0.36f
        val strokeWidth = (minDim * 0.022f).coerceAtLeast(2.5f)

        // =========================================================================
        // LAYER 1: ELEVATED AMBIENT FLOOR REFLECTION GLOW (NO GROUND SHADOW)
        // =========================================================================
        val floorY = cy + radius * 1.18f
        val floorGlowW = radius * 1.60f
        val floorGlowH = radius * 0.30f

        // Soft ambient floor reflection glow that creates pristine 3D elevation
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    corePrimary.copy(alpha = 0.28f),
                    coreAccent.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(cx, floorY),
                radius = floorGlowW / 2f
            ),
            topLeft = Offset(cx - floorGlowW / 2f, floorY - floorGlowH / 2f),
            size = Size(floorGlowW, floorGlowH)
        )

        // =========================================================================
        // LAYER 2: FLOATING DUST PARTICLES
        // =========================================================================
        particles.forEach { p ->
            val pAngle = Math.toRadians((p.angle + Math.toDegrees((particlePhase * p.speedFactor).toDouble())))
            val pDist = radius * p.distFactor
            val px = cx + (pDist * cos(pAngle)).toFloat()
            val py = cy + (pDist * sin(pAngle) * 0.5f).toFloat()
            val zFactor = sin(pAngle).toFloat()
            val pSize = (p.sizeDp.dp.toPx() * (1f + zFactor * 0.25f)).coerceAtLeast(1f)
            val pAlpha = (p.alpha * (0.45f + zFactor * 0.40f)).coerceIn(0.08f, 0.75f)

            drawCircle(
                color = if (zFactor > 0) Color.White.copy(alpha = pAlpha) else corePrimary.copy(alpha = pAlpha * 0.5f),
                radius = pSize,
                center = Offset(px, py)
            )
        }

        // =========================================================================
        // LAYER 3: CRYSTAL GLASS SPHERE SUBSTRATE
        // =========================================================================
        val lightX = cx - radius * 0.38f + cos(phase.toDouble()).toFloat() * (radius * 0.03f)
        val lightY = cy - radius * 0.38f + sin(phase.toDouble()).toFloat() * (radius * 0.03f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.20f),
                    coreDeep,
                    Color(0xFF030712)
                ),
                center = Offset(lightX, lightY),
                radius = radius * 1.20f
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // =========================================================================
        // LAYER 4: BOTTOM ETHEREAL PLASMA MIST
        // =========================================================================
        val mistPath = Path()
        val mistCenter = Offset(
            cx + sin((wavePhase * 0.6f).toDouble()).toFloat() * (radius * 0.05f),
            cy + radius * 0.38f
        )
        val mistPoints = 24

        for (i in 0 until mistPoints) {
            val angle = (i.toFloat() / mistPoints) * 2 * PI
            val h1 = sin(angle * 3 + wavePhase * stateWaveSpeed)
            val h2 = cos(angle * 4 - wavePhase * 1.0f)
            val distortion = (h1 * 0.5f + h2 * 0.5f) * 0.12f
            val mistRadius = radius * 0.75f * (1.0f + distortion.toFloat())

            val x = mistCenter.x + (mistRadius * cos(angle)).toFloat()
            val y = mistCenter.y + (mistRadius * sin(angle)).toFloat()

            if (i == 0) mistPath.moveTo(x, y) else mistPath.lineTo(x, y)
        }
        mistPath.close()

        drawPath(
            path = mistPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    coreAccent.copy(alpha = animMistAlpha),
                    corePrimary.copy(alpha = animMistAlpha * 0.70f),
                    coreSecondary.copy(alpha = animMistAlpha * 0.30f),
                    Color.Transparent
                ),
                center = mistCenter,
                radius = radius * 0.85f
            )
        )

        // Bottom Ground Arc Glow inside Glass Shell
        drawArc(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.80f), corePrimary, Color.Transparent),
                center = Offset(cx, cy + radius * 0.88f),
                radius = radius * 0.60f
            ),
            startAngle = 30f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - radius * 0.60f, cy + radius * 0.60f),
            size = Size(radius * 1.20f, radius * 0.40f),
            style = Stroke(width = strokeWidth * 1.2f)
        )

        // =========================================================================
        // LAYER 5: BRIGHT PURE WHITE GLOWING PILL EYES & REWORKED TALKING MOUTH
        // =========================================================================
        val faceCenter = Offset(
            cx + animOffsetXScale * radius,
            cy - radius * 0.04f + animOffsetYScale * radius
        )
        val eyeSpacing = radius * 0.36f
        val leftEye = faceCenter + Offset(-eyeSpacing, 0f)
        val rightEye = faceCenter + Offset(eyeSpacing, 0f)

        // Smoothly animated eye dimensions
        val blinkFactor = if (state == AiState.IDLE && sin(phase.toDouble()).toFloat() < -0.92f) 0.12f else 1.0f
        val curEyeW = radius * animEyeWScale
        val curEyeH = radius * animEyeHScale * blinkFactor
        val faceStroke = Stroke(width = strokeWidth * 1.20f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Searching Web scanning movement
        val scanOffsetX = if (state == AiState.SEARCHING_WEB) sin((wavePhase * 1.5f).toDouble()).toFloat() * (radius * 0.12f) else 0f
        val finalLeftEye = leftEye + Offset(scanOffsetX, 0f)
        val finalRightEye = rightEye + Offset(scanOffsetX, 0f)

        // Left Eye Glow & Core
        withTransform({
            if (animEyeRot != 0f) rotate(animEyeRot, pivot = finalLeftEye)
        }) {
            if (animSmileCurve > 0.05f) {
                val lPath = Path().apply {
                    moveTo(finalLeftEye.x - curEyeW * 0.8f, finalLeftEye.y + curEyeH * 0.2f)
                    quadraticTo(finalLeftEye.x, finalLeftEye.y - curEyeH * 0.6f * animSmileCurve, finalLeftEye.x + curEyeW * 0.8f, finalLeftEye.y + curEyeH * 0.2f)
                }
                drawPath(lPath, color = Color.White.copy(alpha = animEyeAlpha), style = faceStroke)
            } else {
                drawRoundRect(
                    color = Color.White.copy(alpha = animEyeAlpha),
                    topLeft = finalLeftEye - Offset(curEyeW / 2f, curEyeH / 2f),
                    size = Size(curEyeW, curEyeH),
                    cornerRadius = CornerRadius(curEyeW / 2f, curEyeW / 2f)
                )
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.50f * animEyeAlpha), Color.Transparent),
                        center = finalLeftEye,
                        radius = curEyeW * 1.2f
                    ),
                    topLeft = finalLeftEye - Offset(curEyeW * 0.8f, curEyeH * 0.8f),
                    size = Size(curEyeW * 1.6f, curEyeH * 1.6f),
                    cornerRadius = CornerRadius(curEyeW, curEyeW)
                )
            }
        }

        // Right Eye Glow & Core
        withTransform({
            if (animEyeRot != 0f) rotate(if (state == AiState.EXECUTING || state == AiState.ERROR) -animEyeRot else animEyeRot, pivot = finalRightEye)
        }) {
            if (animSmileCurve > 0.05f) {
                val rPath = Path().apply {
                    moveTo(finalRightEye.x - curEyeW * 0.8f, finalRightEye.y + curEyeH * 0.2f)
                    quadraticTo(finalRightEye.x, finalRightEye.y - curEyeH * 0.6f * animSmileCurve, finalRightEye.x + curEyeW * 0.8f, finalRightEye.y + curEyeH * 0.2f)
                }
                drawPath(rPath, color = Color.White.copy(alpha = animEyeAlpha), style = faceStroke)
            } else {
                drawRoundRect(
                    color = Color.White.copy(alpha = animEyeAlpha),
                    topLeft = finalRightEye - Offset(curEyeW / 2f, curEyeH / 2f),
                    size = Size(curEyeW, curEyeH),
                    cornerRadius = CornerRadius(curEyeW / 2f, curEyeW / 2f)
                )
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.50f * animEyeAlpha), Color.Transparent),
                        center = finalRightEye,
                        radius = curEyeW * 1.2f
                    ),
                    topLeft = finalRightEye - Offset(curEyeW * 0.8f, curEyeH * 0.8f),
                    size = Size(curEyeW * 1.6f, curEyeH * 1.6f),
                    cornerRadius = CornerRadius(curEyeW, curEyeW)
                )
            }
        }

        // Dedicated LISTENING Action: Concentric Audio Input Ripple Arcs below Eyes
        if (state == AiState.LISTENING) {
            val rippleCenter = faceCenter + Offset(0f, radius * 0.32f)
            val audioScale = (audioLevel * 0.8f + 0.25f)
            for (r in 1..3) {
                val arcRadius = radius * (0.15f + r * 0.08f) * audioScale
                val alpha = (0.75f - r * 0.20f).coerceIn(0.1f, 0.9f)
                drawArc(
                    color = Color(0xFF38BDF8).copy(alpha = alpha),
                    startAngle = 30f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(rippleCenter.x - arcRadius, rippleCenter.y - arcRadius * 0.5f),
                    size = Size(arcRadius * 2f, arcRadius),
                    style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round)
                )
            }
        }

        // Dedicated PROCESSING Action: Rotating Loader Ring
        if (state == AiState.PROCESSING) {
            withTransform({
                rotate(degrees = Math.toDegrees((wavePhase * 2f).toDouble()).toFloat(), pivot = faceCenter)
            }) {
                for (i in 0..3) {
                    val angle = i * (PI / 2)
                    val pX = faceCenter.x + cos(angle).toFloat() * (radius * 0.54f)
                    val pY = faceCenter.y + sin(angle).toFloat() * (radius * 0.54f)
                    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = strokeWidth * 0.95f, center = Offset(pX, pY))
                }
            }
        }

        // Dedicated REWORKED TALKING MOUTH: Sleek Emissive Glowing Talking Mouth Curve / Oval
        if (state == AiState.TALKING) {
            val mouthCenter = faceCenter + Offset(0f, radius * 0.28f)
            val voicePulse = abs(sin((wavePhase * 5f).toDouble())).toFloat() * (audioLevel * 0.85f + 0.20f)

            val mouthW = radius * (0.24f + voicePulse * 0.08f)
            val mouthH = radius * (0.05f + voicePulse * 0.16f)

            if (voicePulse > 0.18f) {
                // Expressive Glowing Open Talking Mouth Oval
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.95f),
                    topLeft = mouthCenter - Offset(mouthW / 2f, mouthH / 2f),
                    size = Size(mouthW, mouthH),
                    cornerRadius = CornerRadius(mouthH / 2f, mouthH / 2f)
                )
                // Outer Emissive Mouth Glow
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.60f), Color.Transparent),
                        center = mouthCenter,
                        radius = mouthW * 1.1f
                    ),
                    topLeft = mouthCenter - Offset(mouthW * 0.8f, mouthH * 1.2f),
                    size = Size(mouthW * 1.6f, mouthH * 2.4f),
                    cornerRadius = CornerRadius(mouthH, mouthH)
                )
            } else {
                // Subtle Smiling Mouth Curve on pauses
                val mouthPath = Path().apply {
                    moveTo(mouthCenter.x - mouthW * 0.6f, mouthCenter.y)
                    quadraticTo(
                        mouthCenter.x, mouthCenter.y + radius * 0.08f,
                        mouthCenter.x + mouthW * 0.6f, mouthCenter.y
                    )
                }
                drawPath(mouthPath, color = Color.White, style = faceStroke)
            }
        }

        // =========================================================================
        // LAYER 6: SUBTLE FRESNEL GLASS EDGE RIM & REFRACTION
        // =========================================================================
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.45f),
                    corePrimary.copy(alpha = 0.20f),
                    coreDeep.copy(alpha = 0.50f),
                    Color.White.copy(alpha = 0.25f)
                )
            ),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth * 0.75f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.15f),
                    Color.Black.copy(alpha = 0.40f)
                ),
                center = Offset(cx, cy),
                radius = radius
            ),
            radius = radius * 0.98f,
            center = Offset(cx, cy)
        )

        // =========================================================================
        // LAYER 7: SUBTLE SPECULAR GLASS REFRACTION CAUSTICS
        // =========================================================================
        withTransform({
            rotate(-32f, pivot = Offset(cx - radius * 0.35f, cy - radius * 0.35f))
        }) {
            val hCenter = Offset(cx - radius * 0.38f, cy - radius * 0.38f)
            val hSize = Size(radius * 0.58f, radius * 0.24f)

            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.32f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    )
                ),
                topLeft = hCenter - Offset(hSize.width / 2f, hSize.height / 2f),
                size = hSize
            )
        }

        drawCircle(
            color = Color.White.copy(alpha = 0.38f),
            radius = radius * 0.050f,
            center = Offset(cx - radius * 0.20f, cy - radius * 0.28f)
        )

        withTransform({
            rotate(45f, pivot = Offset(cx + radius * 0.38f, cy + radius * 0.38f))
        }) {
            val rCenter = Offset(cx + radius * 0.38f, cy + radius * 0.38f)
            val rSize = Size(radius * 0.42f, radius * 0.10f)

            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                topLeft = rCenter - Offset(rSize.width / 2f, rSize.height / 2f),
                size = rSize
            )
        }

        // =========================================================================
        // LAYER 8: ACTIVE ENERGY RIPPLE WAVE OVERLAY
        // =========================================================================
        if (state == AiState.TALKING || state == AiState.LISTENING || state == AiState.PROCESSING || state == AiState.THINKING || audioLevel > 0.05f) {
            val waveExpand = (wavePhase / (2 * PI).toFloat())
            val rippleRadius = radius * (1.0f + waveExpand * 0.35f)
            val rippleAlpha = (1.0f - waveExpand).coerceIn(0f, 1f) * 0.45f

            drawCircle(
                color = coreAccent.copy(alpha = rippleAlpha),
                radius = rippleRadius,
                center = Offset(cx, cy),
                style = Stroke(width = strokeWidth * 0.75f)
            )
        }
    }
}

private data class StateColors(
    val primary: Color,
    val secondary: Color,
    val deep: Color,
    val accent: Color
)

private data class ParticleSeed(
    val angle: Float,
    val distFactor: Float,
    val sizeDp: Float,
    val speedFactor: Float,
    val alpha: Float
)

@Preview(showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
fun AiOrbAvatarPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0F19))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Celestia Glass 3D Companion Orb",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val states = AiState.entries.toList().chunked(3)
                states.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { state ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AiOrbAvatar(
                                    state = state,
                                    modifier = Modifier.size(85.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.name,
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
