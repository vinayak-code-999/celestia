package ai.moon.celestia.core.designsystem.component

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A custom Android View that renders a crystal glass 3D AI Orb with dedicated expressions for Listening,
 * Processing, Generating, and Talking embedded inside a translucent 3D glass sphere with elevated floor glow.
 */
class AIAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class AvatarState {
        IDLE,           // Calm blue/cyan, gentle pulse
        CONNECTED,      // Bright teal, steady energy flow
        LISTENING,      // Receptive cyan, listening arcs
        PROCESSING,     // Pulsing amber/orange solar gold
        THINKING,       // Deep purple/indigo nebula
        SEARCHING_WEB,  // Quantum mint scanning
        GENERATING,     // Vibrant emerald green creation
        TALKING,        // Radiant sunset pink speaking
        EXECUTING,      // Energetic cyan cyber pulse
        DISCONNECTED,   // Dim gray/slate
        ERROR           // Pulsing neon red/crimson
    }

    // Current & Target State Configuration
    var currentState: AvatarState = AvatarState.IDLE
        set(value) {
            if (field != value) {
                field = value
                animateToState(value)
            }
        }

    // Animated Properties
    private var coreRadiusFraction = 0.35f
    private var pulseSpeedMultiplier = 1.0f
    private var eyeHeightFraction = 0.36f
    private var eyeWidthFraction = 0.22f
    private var eyeOffsetXFraction = 0f
    private var eyeOffsetYFraction = 0f
    private var mistAlphaFraction = 0.50f

    // Color Palettes
    private var primaryColor = Color.parseColor("#818CF8")    // Indigo
    private var secondaryColor = Color.parseColor("#4338CA")  // Deep Indigo
    private var accentColor = Color.parseColor("#C084FC")     // Periwinkle Accent
    private var deepColor = Color.parseColor("#1E1B4B")       // Dark Midnight Core

    // Target colors for smooth state transition
    private var targetPrimaryColor = primaryColor
    private var targetSecondaryColor = secondaryColor
    private var targetAccentColor = accentColor
    private var targetDeepColor = deepColor

    // Animation & Math Counters
    private var animTime = 0f
    private val animator = ValueAnimator.ofFloat(0f, Float.MAX_VALUE).apply {
        duration = 1_000_000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            animTime += 0.035f
            updateParticles()
            invalidate()
        }
    }

    // Transition Animator
    private var colorAnimator: ValueAnimator? = null

    // Paints & Shaders
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val mistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val faceStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val specularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val mistPath = Path()
    private val facePath = Path()

    // Particles System
    private data class Particle(
        var x: Float,
        var y: Float,
        var radius: Float,
        var alpha: Int,
        var angle: Float,
        var speed: Float,
        var distance: Float
    )

    private val particles = List(16) {
        Particle(
            x = 0f, y = 0f,
            radius = Random.nextFloat() * 3.0f + 1.2f,
            alpha = Random.nextInt(70, 190),
            angle = Random.nextFloat() * 360f,
            speed = Random.nextFloat() * 1.5f + 0.6f,
            distance = Random.nextFloat() * 100f + 40f
        )
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
        colorAnimator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val minDim = Math.min(w, h)
        val floatY = sin(animTime.toDouble()).toFloat() * (minDim * 0.02f)
        val cx = w / 2f
        val cy = h / 2f + floatY
        val radius = minDim * coreRadiusFraction

        // 1. Ambient Elevated Floor Glow (No ground shadow)
        drawElevatedFloorGlow(canvas, cx, cy, radius)

        // 2. Floating 3D Particles
        drawParticles(canvas, cx, cy)

        // 3. 3D Crystal Translucent Glass Core Sphere Substrate
        drawGlassSubstrate(canvas, cx, cy, radius)

        // 4. Bottom Ethereal Plasma Mist
        drawPlasmaMist(canvas, cx, cy, radius)

        // 5. 3D Emissive Glowing LED Facial Features & Actions
        drawFace(canvas, cx, cy, radius)

        // 6. Fresnel Edge Rim Reflection
        drawRim(canvas, cx, cy, radius)

        // 7. Specular Glass Refraction Highlights
        drawSpecularCaustics(canvas, cx, cy, radius)
    }

    private fun drawElevatedFloorGlow(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val floorY = cy + radius * 1.18f
        val floorW = radius * 1.60f
        val floorH = radius * 0.30f

        val shadowGradient = RadialGradient(
            cx, floorY, floorW / 2f,
            intArrayOf(
                adjustAlpha(primaryColor, 0.28f),
                adjustAlpha(accentColor, 0.12f),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.0f, 0.50f, 1.0f),
            Shader.TileMode.CLAMP
        )
        shadowPaint.shader = shadowGradient
        val rect = RectF(cx - floorW / 2f, floorY - floorH / 2f, cx + floorW / 2f, floorY + floorH / 2f)
        canvas.drawOval(rect, shadowPaint)
    }

    private fun drawGlassSubstrate(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val lx = cx - radius * 0.35f
        val ly = cy - radius * 0.35f

        val coreGradient = RadialGradient(
            lx, ly, radius * 1.25f,
            intArrayOf(
                Color.argb(50, 255, 255, 255),
                deepColor,
                Color.parseColor("#030712")
            ),
            floatArrayOf(0.0f, 0.70f, 1.0f),
            Shader.TileMode.CLAMP
        )
        corePaint.shader = coreGradient
        canvas.drawCircle(cx, cy, radius, corePaint)
    }

    private fun drawPlasmaMist(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        mistPath.reset()
        val wavePoints = 24
        val mistCenter = PointF(
            cx + sin(animTime * 0.6f) * (radius * 0.05f),
            cy + radius * 0.38f
        )

        for (i in 0 until wavePoints) {
            val angle = (i.toFloat() / wavePoints) * 2 * PI.toFloat()
            val h1 = sin(angle * 3 + animTime * 1.5f)
            val h2 = cos(angle * 4 - animTime * 1.0f)
            val distortion = (h1 * 0.5f + h2 * 0.5f) * 0.12f
            val mistR = radius * 0.75f * (1.0f + distortion)

            val px = mistCenter.x + mistR * cos(angle)
            val py = mistCenter.y + mistR * sin(angle)

            if (i == 0) mistPath.moveTo(px, py) else mistPath.lineTo(px, py)
        }
        mistPath.close()

        val mistGradient = RadialGradient(
            mistCenter.x, mistCenter.y, radius * 0.85f,
            intArrayOf(
                adjustAlpha(accentColor, mistAlphaFraction),
                adjustAlpha(primaryColor, mistAlphaFraction * 0.70f),
                adjustAlpha(secondaryColor, mistAlphaFraction * 0.30f),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.0f, 0.40f, 0.75f, 1.0f),
            Shader.TileMode.CLAMP
        )
        mistPaint.shader = mistGradient
        canvas.drawPath(mistPath, mistPaint)
    }

    private fun drawFace(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val faceCenter = PointF(
            cx + eyeOffsetXFraction * radius,
            cy - radius * 0.04f + eyeOffsetYFraction * radius
        )
        val eyeSpacing = radius * 0.36f
        val leftEye = PointF(faceCenter.x - eyeSpacing, faceCenter.y)
        val rightEye = PointF(faceCenter.x + eyeSpacing, faceCenter.y)

        val eyeW = radius * eyeWidthFraction
        val blink = if (currentState == AvatarState.IDLE && sin(animTime) < -0.92f) 0.15f else 1.0f
        val eyeH = radius * eyeHeightFraction * blink
        val strokeW = radius * 0.07f
        faceStrokePaint.strokeWidth = strokeW

        facePaint.color = Color.WHITE

        when (currentState) {
            AvatarState.CONNECTED -> {
                val bounceY = sin(animTime * 2f) * (radius * 0.03f)
                facePath.reset()
                facePath.moveTo(leftEye.x - eyeW * 0.8f, leftEye.y + bounceY + eyeH * 0.2f)
                facePath.quadTo(leftEye.x, leftEye.y + bounceY - eyeH * 0.6f, leftEye.x + eyeW * 0.8f, leftEye.y + bounceY + eyeH * 0.2f)
                canvas.drawPath(facePath, faceStrokePaint)

                facePath.reset()
                facePath.moveTo(rightEye.x - eyeW * 0.8f, rightEye.y + bounceY + eyeH * 0.2f)
                facePath.quadTo(rightEye.x, rightEye.y + bounceY - eyeH * 0.6f, rightEye.x + eyeW * 0.8f, rightEye.y + bounceY + eyeH * 0.2f)
                canvas.drawPath(facePath, faceStrokePaint)
            }

            AvatarState.LISTENING -> {
                val lRect = RectF(leftEye.x - eyeW / 2f, leftEye.y - eyeH / 2f, leftEye.x + eyeW / 2f, leftEye.y + eyeH / 2f)
                val rRect = RectF(rightEye.x - eyeW / 2f, rightEye.y - eyeH / 2f, rightEye.x + eyeW / 2f, rightEye.y + eyeH / 2f)

                canvas.drawRoundRect(lRect, eyeW / 2f, eyeW / 2f, facePaint)
                canvas.drawRoundRect(rRect, eyeW / 2f, eyeW / 2f, facePaint)

                // Sound-wave arc ripples below eyes
                val rippleCenterY = cy + radius * 0.32f
                for (r in 1..3) {
                    val arcR = radius * (0.15f + r * 0.08f)
                    val arcAlpha = (200 - r * 50).coerceIn(30, 220)
                    faceStrokePaint.alpha = arcAlpha
                    val oval = RectF(cx - arcR, rippleCenterY - arcR * 0.5f, cx + arcR, rippleCenterY + arcR * 0.5f)
                    canvas.drawArc(oval, 30f, 120f, false, faceStrokePaint)
                }
                faceStrokePaint.alpha = 255
            }

            AvatarState.PROCESSING -> {
                val lRect = RectF(leftEye.x - eyeW / 2f, leftEye.y - eyeH / 2f, leftEye.x + eyeW / 2f, leftEye.y + eyeH / 2f)
                val rRect = RectF(rightEye.x - eyeW / 2f, rightEye.y - eyeH / 2f, rightEye.x + eyeW / 2f, rightEye.y + eyeH / 2f)

                canvas.drawRoundRect(lRect, eyeW / 2f, eyeW / 2f, facePaint)
                canvas.drawRoundRect(rRect, eyeW / 2f, eyeW / 2f, facePaint)

                // Rotating 4-dot loader
                canvas.save()
                canvas.rotate(animTime * 90f, faceCenter.x, faceCenter.y)
                for (i in 0..3) {
                    val angle = i * (PI / 2).toFloat()
                    val px = faceCenter.x + cos(angle) * (radius * 0.54f)
                    val py = faceCenter.y + sin(angle) * (radius * 0.54f)
                    canvas.drawCircle(px, py, strokeW * 0.95f, facePaint)
                }
                canvas.restore()
            }

            AvatarState.TALKING -> {
                val talk = abs(sin(animTime * 4f)) * 0.8f + 0.2f
                val w = eyeW
                val h = eyeH

                val lRect = RectF(leftEye.x - w / 2f, leftEye.y - h / 2f, leftEye.x + w / 2f, leftEye.y + h / 2f)
                val rRect = RectF(rightEye.x - w / 2f, rightEye.y - h / 2f, rightEye.x + w / 2f, rightEye.y + h / 2f)

                canvas.drawRoundRect(lRect, w / 2f, w / 2f, facePaint)
                canvas.drawRoundRect(rRect, w / 2f, w / 2f, facePaint)

                // Reworked Talking Mouth Oval
                val mouthCenter = PointF(faceCenter.x, cy + radius * 0.28f)
                val mouthW = radius * (0.24f + talk * 0.08f)
                val mouthH = radius * (0.05f + talk * 0.16f)

                val mouthRect = RectF(mouthCenter.x - mouthW / 2f, mouthCenter.y - mouthH / 2f, mouthCenter.x + mouthW / 2f, mouthCenter.y + mouthH / 2f)
                canvas.drawRoundRect(mouthRect, mouthH / 2f, mouthH / 2f, facePaint)
            }

            else -> {
                val lRect = RectF(leftEye.x - eyeW / 2f, leftEye.y - eyeH / 2f, leftEye.x + eyeW / 2f, leftEye.y + eyeH / 2f)
                val rRect = RectF(rightEye.x - eyeW / 2f, rightEye.y - eyeH / 2f, rightEye.x + eyeW / 2f, rightEye.y + eyeH / 2f)

                canvas.drawRoundRect(lRect, eyeW / 2f, eyeW / 2f, facePaint)
                canvas.drawRoundRect(rRect, eyeW / 2f, eyeW / 2f, facePaint)
            }
        }
    }

    private fun drawRim(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val rimGradient = LinearGradient(
            cx, cy - radius, cx, cy + radius,
            intArrayOf(
                Color.argb(115, 255, 255, 255),
                adjustAlpha(primaryColor, 0.20f),
                adjustAlpha(deepColor, 0.50f),
                Color.argb(60, 255, 255, 255)
            ),
            floatArrayOf(0.0f, 0.35f, 0.75f, 1.0f),
            Shader.TileMode.CLAMP
        )
        rimPaint.shader = rimGradient
        rimPaint.strokeWidth = radius * 0.05f
        canvas.drawCircle(cx, cy, radius, rimPaint)
    }

    private fun drawSpecularCaustics(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.save()
        canvas.rotate(-32f, cx - radius * 0.38f, cy - radius * 0.38f)

        val hCenter = PointF(cx - radius * 0.38f, cy - radius * 0.38f)
        val hW = radius * 0.58f
        val hH = radius * 0.24f

        val specularGradient = LinearGradient(
            hCenter.x, hCenter.y - hH / 2f, hCenter.x, hCenter.y + hH / 2f,
            intArrayOf(
                Color.argb(80, 255, 255, 255),
                Color.argb(25, 255, 255, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.0f, 0.60f, 1.0f),
            Shader.TileMode.CLAMP
        )
        specularPaint.shader = specularGradient
        val hRect = RectF(hCenter.x - hW / 2f, hCenter.y - hH / 2f, hCenter.x + hW / 2f, hCenter.y + hH / 2f)
        canvas.drawOval(hRect, specularPaint)
        canvas.restore()

        specularPaint.shader = null
        specularPaint.color = Color.WHITE
        specularPaint.alpha = 95
        canvas.drawCircle(cx - radius * 0.20f, cy - radius * 0.28f, radius * 0.05f, specularPaint)
    }

    private fun drawParticles(canvas: Canvas, cx: Float, cy: Float) {
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val px = cx + cos(rad).toFloat() * p.distance
            val py = cy + sin(rad).toFloat() * p.distance * 0.5f

            particlePaint.color = accentColor
            particlePaint.alpha = p.alpha
            canvas.drawCircle(px, py, p.radius, particlePaint)
        }
    }

    private fun updateParticles() {
        particles.forEach { p ->
            p.angle += p.speed * pulseSpeedMultiplier
            if (p.angle >= 360f) p.angle -= 360f
        }
    }

    /**
     * Smoothly transitions avatar colors and dynamics when state changes.
     */
    private fun animateToState(newState: AvatarState) {
        val (pColor, sColor, aColor, dColor, pulseSpd, eyeWFrac, eyeHFrac, mistAlphaFrac) = when (newState) {
            AvatarState.IDLE -> StateConfig(
                "#818CF8", "#4338CA", "#C084FC", "#1E1B4B",
                pulseSpeed = 1.0f, eyeWidthFraction = 0.22f, eyeHeightFraction = 0.36f, mistAlphaFraction = 0.45f
            )
            AvatarState.CONNECTED -> StateConfig(
                "#00F2FE", "#0284C7", "#38BDF8", "#032B45",
                pulseSpeed = 1.2f, eyeWidthFraction = 0.24f, eyeHeightFraction = 0.32f, mistAlphaFraction = 0.65f
            )
            AvatarState.LISTENING -> StateConfig(
                "#00F2FE", "#0D9488", "#34D399", "#022C22",
                pulseSpeed = 1.5f, eyeWidthFraction = 0.24f, eyeHeightFraction = 0.38f, mistAlphaFraction = 0.75f
            )
            AvatarState.PROCESSING -> StateConfig(
                "#FF9100", "#FB8500", "#FEF08A", "#3A1C00",
                pulseSpeed = 2.2f, eyeWidthFraction = 0.28f, eyeHeightFraction = 0.24f, mistAlphaFraction = 0.70f
            )
            AvatarState.THINKING -> StateConfig(
                "#C084FC", "#7E22CE", "#F472B6", "#1E0A38",
                pulseSpeed = 1.5f, eyeWidthFraction = 0.22f, eyeHeightFraction = 0.32f, mistAlphaFraction = 0.75f
            )
            AvatarState.SEARCHING_WEB -> StateConfig(
                "#34D399", "#059669", "#6EE7B7", "#022C22",
                pulseSpeed = 2.0f, eyeWidthFraction = 0.22f, eyeHeightFraction = 0.32f, mistAlphaFraction = 0.60f
            )
            AvatarState.GENERATING -> StateConfig(
                "#10B981", "#059669", "#A7F3D0", "#022016",
                pulseSpeed = 3.0f, eyeWidthFraction = 0.26f, eyeHeightFraction = 0.34f, mistAlphaFraction = 0.85f
            )
            AvatarState.TALKING -> StateConfig(
                "#FF2E93", "#EC4899", "#FDE047", "#250012",
                pulseSpeed = 3.0f, eyeWidthFraction = 0.24f, eyeHeightFraction = 0.36f, mistAlphaFraction = 0.85f
            )
            AvatarState.EXECUTING -> StateConfig(
                "#06B6D4", "#0284C7", "#67E8F9", "#022016",
                pulseSpeed = 3.5f, eyeWidthFraction = 0.20f, eyeHeightFraction = 0.30f, mistAlphaFraction = 0.70f
            )
            AvatarState.DISCONNECTED -> StateConfig(
                "#94A3B8", "#475569", "#E2E8F0", "#0F172A",
                pulseSpeed = 0.3f, eyeWidthFraction = 0.22f, eyeHeightFraction = 0.08f, mistAlphaFraction = 0.20f
            )
            AvatarState.ERROR -> StateConfig(
                "#EF4444", "#DC2626", "#FDBA74", "#2F0404",
                pulseSpeed = 4.0f, eyeWidthFraction = 0.22f, eyeHeightFraction = 0.28f, mistAlphaFraction = 0.80f
            )
        }

        colorAnimator?.cancel()

        val startPrimary = primaryColor
        val startSecondary = secondaryColor
        val startAccent = accentColor
        val startDeep = deepColor
        val startEyeW = eyeWidthFraction
        val startEyeH = eyeHeightFraction
        val startMistAlpha = mistAlphaFraction

        targetPrimaryColor = Color.parseColor(pColor)
        targetSecondaryColor = Color.parseColor(sColor)
        targetAccentColor = Color.parseColor(aColor)
        targetDeepColor = Color.parseColor(dColor)

        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 450
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float
                primaryColor = blendColors(startPrimary, targetPrimaryColor, fraction)
                secondaryColor = blendColors(startSecondary, targetSecondaryColor, fraction)
                accentColor = blendColors(startAccent, targetAccentColor, fraction)
                deepColor = blendColors(startDeep, targetDeepColor, fraction)

                pulseSpeedMultiplier = lerp(pulseSpeedMultiplier, pulseSpd, fraction)
                eyeWidthFraction = lerp(startEyeW, eyeWFrac, fraction)
                eyeHeightFraction = lerp(startEyeH, eyeHFrac, fraction)
                mistAlphaFraction = lerp(startMistAlpha, mistAlphaFrac, fraction)
            }
            start()
        }
    }

    // Helper functions
    private data class StateConfig(
        val primaryHex: String,
        val secondaryHex: String,
        val accentHex: String,
        val deepHex: String,
        val pulseSpeed: Float,
        val eyeWidthFraction: Float,
        val eyeHeightFraction: Float,
        val mistAlphaFraction: Float
    )

    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val a = Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio
        val r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio
        val g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio
        val b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio
        return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction
    }
}
