package com.kylecorry.andromeda.sense.orientation

import android.hardware.SensorManager
import com.kylecorry.sol.math.SolMath.toDegrees

internal class OrientationCalculator {

    private val rotationMatrix = FloatArray(9)
    private val remappedRotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private val sensorValues = FloatArray(4)

    fun getAzimuth(rotation: FloatArray): Float = synchronized(this) {
        if (rotation.size < 9) {
            // It is a rotation vector
            try {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, rotation)
            } catch (e: IllegalArgumentException) {
                // https://groups.google.com/g/android-developers/c/U3N9eL5BcJk - though this should be fixed by now
                System.arraycopy(rotation, 0, sensorValues, 0, 4)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorValues)
            }
            return getAzimuth(rotationMatrix)
        }

        // TODO: If the device is vertical, the AR coordinate space should be used
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