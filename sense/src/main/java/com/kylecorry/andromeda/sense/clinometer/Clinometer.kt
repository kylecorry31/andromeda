package com.kylecorry.andromeda.sense.clinometer

import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.sense.orientation.IOrientationSensor
import com.kylecorry.andromeda.sense.orientation.OrientationUtils
import com.kylecorry.sol.math.SolMath.normalizeAngle
import com.kylecorry.sol.science.geology.Geology

/**
 * A clinometer sensor
 * @param orientationSensor The orientation sensor to use
 * @param isAugmentedReality True if the clinometer should be in AR mode (device held vertically), otherwise the spine of the device is used
 */
class Clinometer(
    private val orientationSensor: IOrientationSensor,
    var isAugmentedReality: Boolean = false
) : AbstractSensor(), IClinometer {

    private var _angle = 0f
    private val rotationMatrix = FloatArray(16)
    private val orientation = FloatArray(3)

    override val angle: Float
        get() = _angle

    override val hasValidReading: Boolean
        get() = orientationSensor.hasValidReading

    override val incline: Float
        get() = Geology.getInclination(_angle)

    override fun startImpl() {
        orientationSensor.start(this::onSensorUpdate)
    }

    override fun stopImpl() {
        orientationSensor.stop(this::onSensorUpdate)
    }

    private fun onSensorUpdate(): Boolean {
        _angle = if (isAugmentedReality) {
            // First get the pitch using the compass orientation - this will tell us which quadrant we're in
            OrientationUtils.getCompassOrientation(
                orientationSensor,
                rotationMatrix,
                orientation
            )
            val pitch = orientation[1]

            // Now get the AR orientation, and adjust it based on the quadrant
            OrientationUtils.getAROrientation(
                orientationSensor,
                rotationMatrix,
                orientation
            )

            if (pitch < 0f){
                // Upright
                normalizeAngle(orientation[1])
            } else {
                // Upside down
                normalizeAngle(-orientation[1] - 180)
            }
        } else {
            OrientationUtils.getAROrientation(
                orientationSensor,
                rotationMatrix,
                orientation
            )
            normalizeAngle(-orientation[2] + 90)
        }
        notifyListeners()
        return true
    }
}