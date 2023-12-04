package com.kylecorry.andromeda.sense.level

import android.view.Surface
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.sense.orientation.IOrientationSensor
import com.kylecorry.andromeda.sense.orientation.OrientationUtils

class Level(
    private val orientationSensor: IOrientationSensor,
    var surfaceRotation: Int = Surface.ROTATION_0,
) :
    AbstractSensor(), ILevel {

    private val rotationMatrix = FloatArray(16)
    private val orientation = FloatArray(3)

    /**
     * The X level in degrees
     */
    override var x: Float = 0f
        private set

    /**
     * The Y level in degrees
     */
    override var y: Float = 0f
        private set

    override val hasValidReading: Boolean
        get() = orientationSensor.hasValidReading

    override fun startImpl() {
        orientationSensor.start(this::onSensorUpdate)
    }

    override fun stopImpl() {
        orientationSensor.stop(this::onSensorUpdate)
    }

    private fun onSensorUpdate(): Boolean {
        OrientationUtils.getCompassOrientation(
            orientationSensor,
            rotationMatrix,
            orientation,
            surfaceRotation
        )

        x = -orientation[2].coerceIn(-90f, 90f)
        y = -orientation[1].coerceIn(-90f, 90f)

        notifyListeners()
        return true
    }
}