package com.kylecorry.andromeda.sense.v2.orientation

import android.hardware.SensorManager
import android.opengl.Matrix
import android.view.Surface
import com.kylecorry.sol.math.MathExtensions.toDegrees
import com.kylecorry.sol.math.Quaternion

internal object OrientationFlowMath {
    fun getOrientation(
        quaternion: Quaternion,
        surfaceRotation: Int = Surface.ROTATION_0,
        augmentedReality: Boolean = false,
        declination: Float = 0f
    ): FloatArray {
        val matrix = getRotationMatrix(quaternion, surfaceRotation, augmentedReality, declination)
        val result = FloatArray(3)
        SensorManager.getOrientation(matrix, result)
        result[0] = result[0].toDegrees()
        result[1] = result[1].toDegrees()
        result[2] = result[2].toDegrees()
        if (augmentedReality) {
            result[1] = -result[1]
            result[2] = -result[2]
        }
        return result
    }

    fun getRotationMatrix(
        quaternion: Quaternion,
        surfaceRotation: Int = Surface.ROTATION_0,
        augmentedReality: Boolean = false,
        declination: Float = 0f
    ): FloatArray {
        val xAxis: Int
        val yAxis: Int
        if (augmentedReality) {
            xAxis = when (surfaceRotation) {
                Surface.ROTATION_90 -> SensorManager.AXIS_Z
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X
                Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Z
                else -> SensorManager.AXIS_X
            }
            yAxis = when (surfaceRotation) {
                Surface.ROTATION_90 -> SensorManager.AXIS_MINUS_X
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_Z
                Surface.ROTATION_270 -> SensorManager.AXIS_X
                else -> SensorManager.AXIS_Z
            }
        } else {
            xAxis = when (surfaceRotation) {
                Surface.ROTATION_90 -> SensorManager.AXIS_Y
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X
                Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y
                else -> SensorManager.AXIS_X
            }
            yAxis = when (surfaceRotation) {
                Surface.ROTATION_90 -> SensorManager.AXIS_MINUS_X
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_Y
                Surface.ROTATION_270 -> SensorManager.AXIS_X
                else -> SensorManager.AXIS_Y
            }
        }

        val matrix = FloatArray(16)
        SensorManager.getRotationMatrixFromVector(matrix, quaternion.toFloatArray())
        if (xAxis != SensorManager.AXIS_X || yAxis != SensorManager.AXIS_Y) {
            SensorManager.remapCoordinateSystem(matrix, xAxis, yAxis, matrix)
        }
        if (declination != 0f) Matrix.rotateM(matrix, 0, declination, 0f, 0f, 1f)
        return matrix
    }
}
