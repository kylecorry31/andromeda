package com.kylecorry.andromeda.sense.orientation.filter

import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.sense.orientation.IOrientationSensor
import com.kylecorry.sol.math.Quaternion

class FilteredOrientationSensor(
    private val sensor: IOrientationSensor,
    private val filter: OrientationSensorFilter,
    private val maintainStateOnRestart: Boolean = false
) : AbstractSensor(), IOrientationSensor {

    private val reading = Quaternion.zero.toFloatArray()
    private var hasReading = false
    private var isFilterInitialized = false

    override fun startImpl() {
        if (!maintainStateOnRestart) {
            Quaternion.zero.toFloatArray().copyInto(reading)
        }
        hasReading = false
        resetSessionMetadata()
        sensor.start(this::onSensorUpdate)
    }

    override fun stopImpl() {
        sensor.stop(this::onSensorUpdate)
    }

    override val hasValidReading: Boolean
        get() = hasReading && sensor.hasValidReading

    override val headingAccuracy: Float?
        get() = null

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() = reading

    private fun onSensorUpdate(): Boolean {
        setEventTimeFrom(sensor)

        if (!isFilterInitialized || (!hasReading && !maintainStateOnRestart)) {
            sensor.rawOrientation.copyInto(reading)
            hasReading = true
            isFilterInitialized = true
            filter.reset(reading)
            notifyListeners()
            return true
        }

        hasReading = true

        val newReading = sensor.rawOrientation.copyOf()
        filter.filter(newReading, reading)

        notifyListeners()
        return true
    }
}
