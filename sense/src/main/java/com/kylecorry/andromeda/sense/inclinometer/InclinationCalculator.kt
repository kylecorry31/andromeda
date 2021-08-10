package com.kylecorry.andromeda.sense.inclinometer

import com.kylecorry.andromeda.core.math.Vector3
import kotlin.math.atan2

internal object InclinationCalculator {

    fun calculate(gravity: Vector3): Float {
        var angle = Math.toDegrees(atan2(gravity.y.toDouble(), gravity.x.toDouble())).toFloat()

        if (angle > 90) {
            angle = 180 - angle
        }

        if (angle < -90) {
            angle = -180 - angle
        }

        return angle
    }

}