package com.kylecorry.andromeda.sense.v2.orientation

import android.content.Context
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.Sensors
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.andromeda.sense.v2.accelerometer.AccelerometerReading
import com.kylecorry.andromeda.sense.v2.accelerometer.GravitySensorFlow
import com.kylecorry.andromeda.sense.v2.accelerometer.LowPassAccelerometerSensorFlow
import kotlin.math.abs
import kotlin.math.withSign
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Estimates portrait, landscape, and flat orientation from gravity or filtered acceleration. */
class DeviceOrientationSensorFlow(
    private val accelerometer: SensorFlow<AccelerometerReading>
) : SensorFlow<DeviceOrientationReading>() {
    constructor(
        context: Context,
        config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(
            sensorDelayUs = SensorManager.SENSOR_DELAY_GAME
        )
    ) : this(
        if (Sensors.hasGravity(context)) GravitySensorFlow(context, config)
        else LowPassAccelerometerSensorFlow(context, config)
    )

    override suspend fun ProducerScope<DeviceOrientationReading>.setupListener() {
        val readingJob = launch {
            accelerometer.flow.collect { reading ->
                val acceleration = reading.acceleration
                val values = floatArrayOf(acceleration.x, acceleration.y, acceleration.z)
                var axis = 0
                for (i in 1..2) {
                    if (abs(values[i]) > abs(values[axis])) axis = i
                }
                val direction = (axis + 1).toDouble().withSign(values[axis].toDouble()).toInt()
                val orientation = when (direction) {
                    -3 -> DeviceOrientationReading.Orientation.FlatInverse
                    -2 -> DeviceOrientationReading.Orientation.PortraitInverse
                    -1 -> DeviceOrientationReading.Orientation.LandscapeInverse
                    1 -> DeviceOrientationReading.Orientation.Landscape
                    2 -> DeviceOrientationReading.Orientation.Portrait
                    else -> DeviceOrientationReading.Orientation.Flat
                }
                trySend(
                    DeviceOrientationReading(
                        eventTime = reading.eventTime,
                        eventTimeElapsedNanos = reading.eventTimeElapsedNanos,
                        orientation = orientation
                    )
                )
            }
        }
        val activeJob = launch { accelerometer.isActive.collect(::setIsActive) }
        awaitClose {
            readingJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }
}
