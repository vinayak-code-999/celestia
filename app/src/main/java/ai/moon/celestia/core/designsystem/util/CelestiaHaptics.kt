package ai.moon.celestia.core.designsystem.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

class CelestiaHapticEngine(
    private val hapticFeedback: HapticFeedback,
    private val vibrator: Vibrator?
) {
    private var lastStreamTickTime: Long = 0L

    @SuppressLint("MissingPermission")
    fun performStreamTick() {
        val now = System.currentTimeMillis()
        if (now - lastStreamTickTime > 75) {
            lastStreamTickTime = now
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            } catch (_: Exception) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun performActionPop() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performTick() {
        performStreamTick()
    }

    fun performActionSuccess() {
        performActionPop()
    }

    @SuppressLint("MissingPermission")
    fun performErrorBuzz() {
        try {
            if (vibrator?.hasVibrator() == true) {
                val timings = longArrayOf(0, 50, 50, 50)
                val amplitudes = intArrayOf(0, 200, 0, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberCelestiaHaptics(): CelestiaHapticEngine {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    return remember(hapticFeedback, vibrator) {
        CelestiaHapticEngine(hapticFeedback, vibrator)
    }
}
