package com.kylecorry.andromeda.sense.clinometer

import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.sense.orientation.IOrientationSensor
import com.kylecorry.andromeda.sense.orientation.OrientationUtils
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
            OrientationUtils.getAROrientation(
                orientationSensor,
                rotationMatrix,
                orientation
            )
            orientation[1]
        } else {
            OrientationUtils.getCompassOrientation(
                orientationSensor,
                rotationMatrix,
                orientation
            )
            -orientation[1]
        }
        notifyListeners()
        return true
    }
}