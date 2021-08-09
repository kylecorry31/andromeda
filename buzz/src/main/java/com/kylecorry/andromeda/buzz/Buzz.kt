package com.kylecorry.andromeda.buzz

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import java.time.Duration

class Buzz(context: Context) : IBuzz {
    private val vibrator = context.getSystemService<Vibrator>()

    private fun waveform(millis: List<Long>, repeatPosition: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(millis.toLongArray(), repeatPosition))
        } else {
            vibrator?.vibrate(millis.toLongArray(), repeatPosition)
        }
    }

    private fun waveform(millis: List<Long>, amplitudes: List<Int>, repeatPosition: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    millis.toLongArray(),
                    amplitudes.toIntArray(),
                    repeatPosition
                )
            )
        } else {
            vibrator?.vibrate(millis.toLongArray(), repeatPosition)
        }
    }

    private fun oneShot(millis: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(millis, amplitude))
        } else {
            vibrator?.vibrate(millis)
        }
    }

    override fun on(amplitude: Int) {
        oneShot(Duration.ofDays(1).toMillis(), amplitude)
    }

    override fun feedback(feedbackType: HapticFeedbackType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effectId = when (feedbackType) {
                HapticFeedbackType.Tick -> VibrationEffect.EFFECT_TICK
                HapticFeedbackType.Click -> VibrationEffect.EFFECT_CLICK
                HapticFeedbackType.HeavyClick -> VibrationEffect.EFFECT_HEAVY_CLICK
                HapticFeedbackType.DoubleClick -> VibrationEffect.EFFECT_DOUBLE_CLICK
            }
            vibrator?.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            val vibrationPattern: List<Long> = when (feedbackType) {
                HapticFeedbackType.Tick -> listOf(125, 30)
                HapticFeedbackType.Click -> listOf(0, 10, 20, 30)
                HapticFeedbackType.HeavyClick -> listOf(0, 1, 20, 21)
                HapticFeedbackType.DoubleClick -> listOf(0, 30, 100, 30)
            }
            pattern(vibrationPattern.map { Duration.ofMillis(it) })
        }
    }

    override fun off() {
        vibrator?.cancel()
    }

    override fun once(onDuration: Duration, amplitude: Int) {
        oneShot(onDuration.toMillis(), amplitude)
    }

    override fun interval(onDuration: Duration, offDuration: Duration, amplitude: Int) {
        pattern(listOf(onDuration, offDuration), listOf(amplitude, 0), true)
    }

    override fun pattern(durations: List<Duration>, amplitudes: List<Int>?, repeat: Boolean) {
        if (amplitudes != null) {
            waveform(durations.map { it.toMillis() }, amplitudes, if (repeat) 0 else -1)
        } else {
            waveform(durations.map { it.toMillis() }, if (repeat) 0 else -1)
        }
    }

    override fun isAvailable(): Boolean {
        return vibrator?.hasVibrator() == true
    }
}