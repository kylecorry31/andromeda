package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.accelerometer.AccelerometerReading
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.withSign

/** Estimates portrait, landscape, and flat orientation from gravity or filtered acceleration. */
class DeviceOrientationSensorFlow(
    private val accelerometer: SensorFlow<AccelerometerReading>
) : SensorFlow<DeviceOrientationReading>() {
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
