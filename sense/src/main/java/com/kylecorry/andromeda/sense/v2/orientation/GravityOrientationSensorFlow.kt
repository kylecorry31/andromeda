package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.accelerometer.AccelerometerReading
import com.kylecorry.andromeda.sense.v2.accelerometer.GravitySensorFlow
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.sign

/** Calculates a gravity aligned orientation from an accelerometer source. Heading is fixed to zero. */
class GravityOrientationSensorFlow(
    private val accelerometer: SensorFlow<AccelerometerReading>
) : SensorFlow<QuaternionOrientationReading>() {
    constructor(flow: GravitySensorFlow) : this(flow as SensorFlow<AccelerometerReading>)

    override suspend fun ProducerScope<QuaternionOrientationReading>.setupListener() {
        val readingJob = launch {
            accelerometer.flow.collect { reading ->
                val gravity = reading.acceleration
                val magneticField = floatArrayOf(
                    gravity.x * sign(gravity.z) * sign(gravity.y),
                    gravity.y * sign(gravity.z) * sign(gravity.y),
                    0f
                )
                val quaternion = OrientationQuaternionMath.fromGravityAndMagneticField(
                    floatArrayOf(gravity.x, gravity.y, gravity.z),
                    magneticField
                )
                trySend(
                    QuaternionOrientationReading(
                        eventTime = reading.eventTime,
                        eventTimeElapsedNanos = reading.eventTimeElapsedNanos,
                        quaternion = quaternion,
                        accuracy = reading.accuracy
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
