package com.kylecorry.andromeda.sense.v2.accelerometer

import android.opengl.Matrix
import android.view.Surface
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.orientation.OrientationFlowMath
import com.kylecorry.andromeda.sense.v2.readings.QuaternionReading
import com.kylecorry.andromeda.sense.v2.readings.SensorAccuracyReading
import com.kylecorry.sol.math.Vector3
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WorldAccelerometerSensorFlow<T>(
    private val accelerometer: SensorFlow<AccelerometerReading>,
    private val orientation: SensorFlow<T>,
    var useTrueNorth: Boolean = false,
    var surfaceRotation: Int = Surface.ROTATION_0,
    var useAROrientation: Boolean = false,
    var declination: Float = 0f
) : SensorFlow<AccelerometerReading>() where T : SensorAccuracyReading, T : QuaternionReading {

    override suspend fun ProducerScope<AccelerometerReading>.setupListener() {
        val readingJob = launch {
            combine(accelerometer.flow, orientation.flow) { acceleration, orientationReading ->
                val matrix = OrientationFlowMath.getRotationMatrix(
                    orientationReading.quaternion,
                    surfaceRotation,
                    useAROrientation,
                    if (useTrueNorth) declination else 0f
                )
                Matrix.invertM(matrix, 0, matrix, 0)
                val vector = acceleration.acceleration
                val transformed = floatArrayOf(vector.x, vector.y, vector.z, 0f)
                Matrix.multiplyMV(transformed, 0, matrix, 0, transformed, 0)
                val latest = if (acceleration.eventTimeElapsedNanos >= orientationReading.eventTimeElapsedNanos) {
                    acceleration
                } else {
                    orientationReading
                }
                AccelerometerReading(
                    eventTime = latest.eventTime,
                    eventTimeElapsedNanos = latest.eventTimeElapsedNanos,
                    acceleration = Vector3(transformed[0], transformed[1], transformed[2]),
                    accuracy = if (acceleration.accuracy.ordinal <= orientationReading.accuracy.ordinal) {
                        acceleration.accuracy
                    } else {
                        orientationReading.accuracy
                    }
                )
            }.collect { trySend(it) }
        }
        val activeJob = launch {
            combine(accelerometer.isActive, orientation.isActive) { accel, orient -> accel || orient }
                .collect(::setIsActive)
        }
        awaitClose {
            readingJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }
}
