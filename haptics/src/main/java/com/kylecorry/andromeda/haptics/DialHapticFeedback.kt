package com.kylecorry.andromeda.haptics

import com.kylecorry.sol.math.SolMath.deltaAngle
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class DialHapticFeedback(
    private val hapticMotor: IHapticMotor,
    private val vibrateFrequency: Int = 1,
    private val atAngleThreshold: Float = 0.25f
) {

    private var lastVibrate = 0f

    var angle: Float = 0f
        set(value) {
            if (shouldVibrate(value, lastVibrate)) {
                hapticMotor.feedback(HapticFeedbackType.Tick)
                lastVibrate = value
            }
            field = value
        }

    private fun shouldVibrate(current: Float, last: Float): Boolean {
        val currInt = current.roundToInt() % 360
        val lastInt = last.roundToInt() % 360

        return currInt % vibrateFrequency == 0 && currInt != lastInt && deltaAngle(
            current,
            last
        ).absoluteValue > atAngleThreshold
    }


    fun stop() {
        hapticMotor.off()
    }

}