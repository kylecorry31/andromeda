package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.accelerometer.AccelerometerReading
import com.kylecorry.andromeda.sense.v2.accelerometer.AccelerometerSensorFlow
import com.kylecorry.andromeda.sense.v2.magnetometer.MagnetometerReading
import com.kylecorry.andromeda.sense.v2.magnetometer.MagnetometerSensorFlow
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Calculates orientation from accelerometer and magnetometer readings. */
class CustomGeomagneticOrientationSensorFlow(
    private val magnetometer: SensorFlow<MagnetometerReading>,
    private val accelerometer: SensorFlow<AccelerometerReading>,
    private val onlyUseMagnetometerAccuracy: Boolean = false
) : SensorFlow<QuaternionOrientationReading>() {

    constructor(
        magnetometer: MagnetometerSensorFlow,
        accelerometer: AccelerometerSensorFlow,
        onlyUseMagnetometerAccuracy: Boolean = false
    ) : this(
        magnetometer as SensorFlow<MagnetometerReading>,
        accelerometer as SensorFlow<AccelerometerReading>,
        onlyUseMagnetometerAccuracy
    )

    override suspend fun ProducerScope<QuaternionOrientationReading>.setupListener() {
        val readingJob = launch {
            combine(magnetometer.flow, accelerometer.flow) { magnetic, acceleration ->
                val orientation = OrientationQuaternionMath.fromGravityAndMagneticField(
                    floatArrayOf(
                        acceleration.acceleration.x,
                        acceleration.acceleration.y,
                        acceleration.acceleration.z
                    ),
                    floatArrayOf(
                        magnetic.magneticField.x,
                        magnetic.magneticField.y,
                        magnetic.magneticField.z
                    )
                )
                val latest = if (magnetic.eventTimeElapsedNanos >= acceleration.eventTimeElapsedNanos) {
                    magnetic
                } else {
                    acceleration
                }
                QuaternionOrientationReading(
                    eventTime = latest.eventTime,
                    eventTimeElapsedNanos = latest.eventTimeElapsedNanos,
                    quaternion = orientation,
                    accuracy = if (onlyUseMagnetometerAccuracy) magnetic.accuracy else
                        OrientationQuaternionMath.minAccuracy(magnetic.accuracy, acceleration.accuracy)
                )
            }.collect { trySend(it) }
        }
        val activeJob = launch {
            combine(magnetometer.isActive, accelerometer.isActive) { mag, accel -> mag || accel }
                .collect(::setIsActive)
        }
        awaitClose {
            readingJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }
}
