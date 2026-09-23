package com.kylecorry.andromeda.sense.v2.orientation

import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import kotlin.math.sqrt

internal object OrientationQuaternionMath {
    fun fromGravityAndMagneticField(gravity: FloatArray, magneticField: FloatArray): Quaternion {
        val matrix = FloatArray(16)
        SensorManager.getRotationMatrix(matrix, null, gravity, magneticField)
        val trace = matrix[0] + matrix[5] + matrix[10]
        val r = sqrt(1f + trace)
        val s = 1f / (2f * r)
        val quaternion = floatArrayOf(
            (matrix[6] - matrix[9]) * s,
            (matrix[8] - matrix[2]) * s,
            (matrix[1] - matrix[4]) * s,
            r / 2f
        )
        QuaternionMath.inverse(quaternion, quaternion)
        return Quaternion.from(quaternion)
    }

    fun minAccuracy(a: SensorAccuracy, b: SensorAccuracy): SensorAccuracy =
        if (a.ordinal <= b.ordinal) a else b
}
