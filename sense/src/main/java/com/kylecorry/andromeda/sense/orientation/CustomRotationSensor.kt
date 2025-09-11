package com.kylecorry.andromeda.sense.orientation

import android.util.Log
import android.util.Range
import com.kylecorry.andromeda.core.coroutines.onMain
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.core.subscriptions.Subscription
import com.kylecorry.andromeda.sense.accelerometer.IAccelerometer
import com.kylecorry.andromeda.sense.magnetometer.IMagnetometer
import com.kylecorry.luna.coroutines.CoroutineQueueRunner
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.math.Vector3Utils
import com.kylecorry.sol.science.geology.Geology
import kotlin.math.absoluteValue
import kotlin.math.min

class CustomRotationSensor(
    private val magnetometer: IMagnetometer,
    private val accelerometer: IAccelerometer,
    private val gyro: IGyroscope,
    private val gyroWeight: Float = 0.998f,
    private val outOfSyncDotThreshold: Float = 0.85f,
    private val outOfSyncMillisThreshold: Long = 1000,
    private val validMagnetometerMagnitudes: Range<Float> = Range(10f, 100f),
    private val validAccelerometerMagnitudes: Range<Float> = Range(
        0.1f * Geology.GRAVITY,
        2f * Geology.GRAVITY
    ),
    private val onlyUseMagnetometerQuality: Boolean = false,
    private val verbose: Boolean = false
) : AbstractSensor(), IOrientationSensor {

    private val _quaternion = Quaternion.zero.toFloatArray()
    private val lock = Object()

    private val runner = CoroutineQueueRunner()

    private val pendingChangesSubscription = Subscription()

    private val geomagneticOrientationSensor =
        CustomGeomagneticRotationSensor(magnetometer, accelerometer, onlyUseMagnetometerQuality)

    override fun startImpl() {
        isInitialized = false
        geomagneticCount = 0
        gyroCount = 0
        pendingChangesSubscription.subscribe(this::update)
        geomagneticOrientationSensor.start(this::onSensorUpdate)
        gyro.start(this::onGyroUpdate)
    }

    override fun stopImpl() {
        geomagneticOrientationSensor.stop(this::onSensorUpdate)
        gyro.stop(this::onGyroUpdate)
        runner.cancel()
        pendingChangesSubscription.unsubscribe(this::update)
    }

    private var isInitialized = false
    private var geomagneticCount = 0
    private var gyroCount = 0

    private val minCount = 5

    private var lastGyro = Quaternion.zero.toFloatArray()

    private val temp = FloatArray(4)
    private val magQuaternion = Quaternion.zero.toFloatArray()
    private val gyroQuaternion = Quaternion.zero.toFloatArray()
    private val previousQuaternion = Quaternion.zero.toFloatArray()

    private var outOfSyncTime = 0L

    private suspend fun update() {
        synchronized(lock) {
            if (!hasValidReading) {
                return
            }

            _quaternion.copyInto(previousQuaternion)

            updateMagQuaternion()
            updateGyroQuaternion()

            // If there's a really high magnetic field or acceleration, use the gyro only
            val dot = QuaternionMath.dot(gyroQuaternion, magQuaternion)

            // Complementary filter
            val alpha =
                if (isInitialized && dot.absoluteValue < outOfSyncDotThreshold) {
                    if (outOfSyncTime == 0L) {
                        outOfSyncTime = System.currentTimeMillis()
                    }

                    // If there's a large acceleration or magnetic field, use the gyro only
                    val magneticMagnitude = Vector3Utils.magnitude(magnetometer.rawMagneticField)
                    val accelerationMagnitude =
                        Vector3Utils.magnitude(accelerometer.rawAcceleration)

                    val outOfSyncDuration = System.currentTimeMillis() - outOfSyncTime

                    if (outOfSyncDuration < outOfSyncMillisThreshold) {
                        // The sensors recently went out of sync, the gyro is likely more accurate
                        if (verbose) {
                            Log.d("CustomRotationSensor", "Out of sync - recent")
                        }
                        1f
                    } else if (!validMagnetometerMagnitudes.contains(magneticMagnitude) ||
                        !validAccelerometerMagnitudes.contains(accelerationMagnitude)
                    ) {
                        // The geomagnetic sensor is likely out of sync, use the gyro
                        if (verbose) {
                            Log.d(
                                "CustomRotationSensor",
                                "Out of sync - magnetic or acceleration out of sync"
                            )
                        }
                        1f
                    } else {
                        // The gyro is likely out of sync, use the geomagnetic sensor
                        if (verbose) {
                            Log.d("CustomRotationSensor", "Out of sync - gyro out of sync")
                        }
                        0f
                    }
                } else if (isInitialized) {
                    outOfSyncTime = 0L
                    gyroWeight
                } else {
                    outOfSyncTime = 0L
                    isInitialized = true
                    0f
                }

            QuaternionMath.lerp(magQuaternion, gyroQuaternion, alpha, _quaternion)

            // Prevent NaN from ruining everything
            if (isInvalid(_quaternion)) {
                if (verbose) {
                    Log.d("CustomRotationSensor", "NaN detected, resetting to previous quaternion")
                }
                previousQuaternion.copyInto(_quaternion)
            }
        }

        onMain {
            notifyListeners()
        }
    }

    private fun isInvalid(quaternion: FloatArray): Boolean {
        return quaternion.any { it.isNaN() || it.isInfinite() }
    }

    private fun updateGyroQuaternion() {
        // Get the change from the gyro
        QuaternionMath.subtractRotation(gyro.rawOrientation, lastGyro, temp)
        gyro.rawOrientation.copyInto(lastGyro)

        // Rotate the current orientation by the change in gyro
        QuaternionMath.multiply(_quaternion, temp, gyroQuaternion)

        QuaternionMath.normalize(gyroQuaternion, gyroQuaternion)
    }

    private fun updateMagQuaternion() {
        geomagneticOrientationSensor.rawOrientation.copyInto(magQuaternion)
    }

    private fun onSensorUpdate(): Boolean {
        if (geomagneticCount < minCount) {
            geomagneticCount++
        }
        return true
    }

    private fun onGyroUpdate(): Boolean {
        if (gyroCount < minCount) {
            gyroCount++

            if (gyroCount == minCount) {
                gyro.rawOrientation.copyInto(lastGyro)
            }

        }
        pendingChangesSubscription.publish()
        return true
    }

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() {
            return synchronized(lock) {
                if (geomagneticCount < minCount || gyroCount < minCount) {
                    geomagneticOrientationSensor.rawOrientation
                } else {
                    _quaternion
                }
            }
        }

    override val headingAccuracy: Float?
        get() = null

    override val hasValidReading: Boolean
        get() = geomagneticOrientationSensor.hasValidReading && gyro.hasValidReading && geomagneticCount >= minCount && gyroCount >= minCount

    override val quality: Quality
        get() = if (onlyUseMagnetometerQuality) {
            geomagneticOrientationSensor.quality
        } else {
            Quality.entries[min(
                geomagneticOrientationSensor.quality.ordinal,
                gyro.quality.ordinal
            )]
        }
}