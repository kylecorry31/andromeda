package com.kylecorry.andromeda.sense.v2.orientation

import com.kylecorry.andromeda.sense.v2.SensorFlow
import com.kylecorry.andromeda.sense.v2.accelerometer.AccelerometerReading
import com.kylecorry.andromeda.sense.v2.gyroscope.GyroscopeOrientationReading
import com.kylecorry.andromeda.sense.v2.magnetometer.MagnetometerReading
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.science.geophysics.Geophysics
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/** Fuses gyroscope orientation with the accelerometer and magnetometer orientation. */
class CustomRotationSensorFlow(
    private val magnetometer: SensorFlow<MagnetometerReading>,
    private val accelerometer: SensorFlow<AccelerometerReading>,
    private val gyroscope: SensorFlow<GyroscopeOrientationReading>,
    private val gyroWeight: Float = 0.998f,
    private val outOfSyncDotThreshold: Float = 0.85f,
    private val outOfSyncMillisThreshold: Long = 1000,
    private val validMagnetometerMagnitudes: ClosedFloatingPointRange<Float> = 10f..100f,
    private val validAccelerometerMagnitudes: ClosedFloatingPointRange<Float> =
        (0.1f * Geophysics.GRAVITY)..(2f * Geophysics.GRAVITY),
    private val onlyUseMagnetometerAccuracy: Boolean = false,
    private val maintainStateOnRestart: Boolean = false
) : SensorFlow<QuaternionOrientationReading>() {
    private val orientation = Quaternion.zero.toFloatArray()

    private val geomagnetic = CustomGeomagneticOrientationSensorFlow(
        magnetometer,
        accelerometer,
        onlyUseMagnetometerAccuracy
    )

    override suspend fun ProducerScope<QuaternionOrientationReading>.setupListener() {
        var geomagneticCount = 0
        var gyroscopeCount = 0
        var lastGeomagnetic: QuaternionOrientationReading? = null
        var outOfSyncTime = 0L
        if (!maintainStateOnRestart) Quaternion.zero.toFloatArray().copyInto(orientation)
        val lastGyro = Quaternion.zero.toFloatArray()
        val gyroOrientation = Quaternion.zero.toFloatArray()
        val delta = Quaternion.zero.toFloatArray()
        val previous = Quaternion.zero.toFloatArray()
        var initialized = maintainStateOnRestart

        val geomagneticJob = launch {
            geomagnetic.flow.collect { reading ->
                lastGeomagnetic = reading
                if (geomagneticCount < MIN_COUNT) geomagneticCount++
            }
        }
        val gyroscopeJob = launch {
            gyroscope.flow.collect { reading ->
                if (gyroscopeCount < MIN_COUNT) {
                    gyroscopeCount++
                    if (gyroscopeCount == MIN_COUNT) reading.quaternion.toFloatArray().copyInto(lastGyro)
                }
                if (geomagneticCount < MIN_COUNT || gyroscopeCount < MIN_COUNT) return@collect

                val magnetic = lastGeomagnetic ?: return@collect
                val acceleration = accelerometer.lastKnown.value ?: return@collect
                val magneticField = magnetometer.lastKnown.value ?: return@collect

                orientation.copyInto(previous)
                QuaternionMath.subtractRotation(reading.quaternion.toFloatArray(), lastGyro, delta)
                reading.quaternion.toFloatArray().copyInto(lastGyro)
                QuaternionMath.multiply(orientation, delta, gyroOrientation)
                QuaternionMath.normalize(gyroOrientation, gyroOrientation)

                val magOrientation = magnetic.quaternion.toFloatArray()
                val dot = QuaternionMath.dot(gyroOrientation, magOrientation)
                val alpha = if (initialized && abs(dot) < outOfSyncDotThreshold) {
                    if (outOfSyncTime == 0L) outOfSyncTime = System.currentTimeMillis()
                    val duration = System.currentTimeMillis() - outOfSyncTime
                    val magMagnitude = magnitude(
                        magneticField.magneticField.x,
                        magneticField.magneticField.y,
                        magneticField.magneticField.z
                    )
                    val accelMagnitude = magnitude(
                        acceleration.acceleration.x,
                        acceleration.acceleration.y,
                        acceleration.acceleration.z
                    )
                    if (duration < outOfSyncMillisThreshold ||
                        magMagnitude !in validMagnetometerMagnitudes ||
                        accelMagnitude !in validAccelerometerMagnitudes
                    ) 1f else 0f
                } else if (initialized) {
                    outOfSyncTime = 0L
                    gyroWeight
                } else {
                    outOfSyncTime = 0L
                    initialized = true
                    0f
                }

                QuaternionMath.lerp(magOrientation, gyroOrientation, alpha, orientation)
                if (orientation.any { it.isNaN() || it.isInfinite() }) previous.copyInto(orientation)

                val latest = if (reading.eventTimeElapsedNanos >= magnetic.eventTimeElapsedNanos) reading else magnetic
                trySend(
                    QuaternionOrientationReading(
                        eventTime = latest.eventTime,
                        eventTimeElapsedNanos = latest.eventTimeElapsedNanos,
                        quaternion = Quaternion.from(orientation),
                        accuracy = if (onlyUseMagnetometerAccuracy) magnetic.accuracy else
                            OrientationQuaternionMath.minAccuracy(magnetic.accuracy, reading.accuracy)
                    )
                )
            }
        }
        val activeJob = launch {
            combine(geomagnetic.isActive, gyroscope.isActive) { mag, gyro -> mag || gyro }
                .collect(::setIsActive)
        }
        awaitClose {
            geomagneticJob.cancel()
            gyroscopeJob.cancel()
            activeJob.cancel()
            setIsActive(false)
        }
    }

    private fun magnitude(x: Float, y: Float, z: Float) = sqrt(x * x + y * y + z * z)

    companion object {
        private const val MIN_COUNT = 5
    }
}
