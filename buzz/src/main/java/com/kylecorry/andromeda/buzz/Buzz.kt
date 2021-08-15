package com.kylecorry.andromeda.buzz

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.permissions.Permissions
import java.time.Duration

object Buzz {

    /**
     * Turns the vibrator on (note: maximum 1 day)
     * @param amplitude the amplitude between 0 and 255, or -1 for the default amplitude
     */
    fun on(context: Context, amplitude: Int = -1) {
        oneShot(context, Duration.ofDays(1).toMillis(), amplitude)
    }

    /**
     * Performs the specified haptic feedback
     */
    @SuppressLint("MissingPermission")
    fun feedback(context: Context, feedbackType: HapticFeedbackType) {
        if (!Permissions.canVibrate(context)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effectId = when (feedbackType) {
                HapticFeedbackType.Tick -> VibrationEffect.EFFECT_TICK
                HapticFeedbackType.Click -> VibrationEffect.EFFECT_CLICK
                HapticFeedbackType.HeavyClick -> VibrationEffect.EFFECT_HEAVY_CLICK
                HapticFeedbackType.DoubleClick -> VibrationEffect.EFFECT_DOUBLE_CLICK
            }
            getVibrator(context)?.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            val vibrationPattern: List<Long> = when (feedbackType) {
                HapticFeedbackType.Tick -> listOf(125, 30)
                HapticFeedbackType.Click -> listOf(0, 10, 20, 30)
                HapticFeedbackType.HeavyClick -> listOf(0, 1, 20, 21)
                HapticFeedbackType.DoubleClick -> listOf(0, 30, 100, 30)
            }
            pattern(context, vibrationPattern.map { Duration.ofMillis(it) })
        }
    }

    /**
     * Turns the vibrator off
     */
    @SuppressLint("MissingPermission")
    fun off(context: Context) {
        if (!Permissions.canVibrate(context)) {
            return
        }
        getVibrator(context)?.cancel()
    }

    /**
     * Turns the vibrator on for a duration
     * @param onDuration the duration to turn the vibrator on for
     * @param amplitude the amplitude between 0 and 255, or -1 for the default amplitude
     */
    fun once(context: Context, onDuration: Duration, amplitude: Int = -1) {
        oneShot(context, onDuration.toMillis(), amplitude)
    }

    /**
     * Turns the vibrator on and off indefinitely (starts on)
     * @param onDuration the duration to turn the vibrator on for
     * @param offDuration the duration to turn the vibrator off for
     * @param amplitude the amplitude between 0 and 255, or -1 for the default amplitude
     */
    fun interval(
        context: Context,
        onDuration: Duration,
        offDuration: Duration,
        amplitude: Int = -1
    ) {
        pattern(context, listOf(onDuration, offDuration), listOf(amplitude, 0), true)
    }

    /**
     * Turns the vibrator on and off based on a pattern (starts off by default, override with amplitudes)
     * @param durations the duration to turn the vibrator on or off for
     * @param amplitudes the amplitudes between 0 and 255, or -1 for the default amplitude. Must correspond with the durations array.
     * @param repeat true to repeat indefinitely, defaults to false
     */
    fun pattern(
        context: Context,
        durations: List<Duration>,
        amplitudes: List<Int>? = null,
        repeat: Boolean = false
    ) {
        if (amplitudes != null) {
            waveform(context, durations.map { it.toMillis() }, amplitudes, if (repeat) 0 else -1)
        } else {
            waveform(context, durations.map { it.toMillis() }, if (repeat) 0 else -1)
        }
    }

    /**
     * Determines if the device has a vibrator
     * @return true if a vibrator exists, false otherwise
     */
    fun isAvailable(context: Context): Boolean {
        return getVibrator(context)?.hasVibrator() == true
    }

    private fun getVibrator(context: Context): Vibrator? {
        return context.getSystemService()
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun waveform(context: Context, millis: List<Long>, repeatPosition: Int = -1) {
        if (!Permissions.canVibrate(context)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context)?.vibrate(
                VibrationEffect.createWaveform(
                    millis.toLongArray(),
                    repeatPosition
                )
            )
        } else {
            getVibrator(context)?.vibrate(millis.toLongArray(), repeatPosition)
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun waveform(
        context: Context,
        millis: List<Long>,
        amplitudes: List<Int>,
        repeatPosition: Int = -1
    ) {
        if (!Permissions.canVibrate(context)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context)?.vibrate(
                VibrationEffect.createWaveform(
                    millis.toLongArray(),
                    amplitudes.toIntArray(),
                    repeatPosition
                )
            )
        } else {
            getVibrator(context)?.vibrate(millis.toLongArray(), repeatPosition)
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun oneShot(context: Context, millis: Long, amplitude: Int) {
        if (!Permissions.canVibrate(context)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getVibrator(context)?.vibrate(VibrationEffect.createOneShot(millis, amplitude))
        } else {
            getVibrator(context)?.vibrate(millis)
        }
    }
}