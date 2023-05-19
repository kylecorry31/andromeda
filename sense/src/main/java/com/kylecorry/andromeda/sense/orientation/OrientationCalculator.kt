package com.kylecorry.andromeda.sense.orientation

import android.hardware.SensorManager
import com.kylecorry.sol.math.SolMath.toDegrees

internal class OrientationCalculator {

    private val rotationMatrix = FloatArray(9)
    private val remappedRotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    fun getAzimuth(rotation: FloatArray): Float = synchronized(this) {
        if (rotation.size != 9) {
            // It is a rotation vector
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotation)
            return getAzimuth(rotationMatrix)
        }

        SensorManager.remapCoordinateSystem(
            rotation,
            SensorManager.AXIS_Y,
            SensorManager.AXIS_MINUS_X,
            remappedRotationMatrix
        )
        SensorManager.getOrientation(remappedRotationMatrix, orientation)
        return orientation[0].toDegrees() - 90f
    }
}