package com.kylecorry.andromeda.sense.clinometer

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.clinometer.Clinometer
import com.kylecorry.sol.math.SolMath.toDegrees
import com.kylecorry.sol.math.SolMath.wrap
import com.kylecorry.sol.math.Vector3
import kotlin.math.atan2

class SideClinometer(context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST) :
    Clinometer(context, sensorDelay) {
    override fun calculateUnitAngle(gravity: Vector3): Float {
        return wrap(
            atan2(gravity.x.toDouble(), gravity.y.toDouble()).toDegrees().toFloat() + 90f,
            0f,
            360f
        )
    }
}