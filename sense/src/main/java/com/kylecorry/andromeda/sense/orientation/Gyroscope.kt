package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.andromeda.core.math.Euler
import com.kylecorry.andromeda.core.math.Quaternion
import com.kylecorry.andromeda.core.math.QuaternionMath
import com.kylecorry.andromeda.core.math.toDegrees
import com.kylecorry.andromeda.sense.BaseSensor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Gyroscope(context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST, private val threshold: Float = 0.00001f) :
    BaseSensor(context, Sensor.TYPE_GYROSCOPE, sensorDelay),
    IGyroscope {

    override val angularRate: Euler
        get() {
            return synchronized(lock) {
                Euler.from(_angularRate)
            }
        }

    override val rawAngularRate: FloatArray
        get() = synchronized(lock) {
            _angularRate.clone()
        }

    override val orientation: Quaternion
        get() = Quaternion.from(rawOrientation)

    override val rawOrientation: FloatArray
        get() {
            return synchronized(lock) {
                _quaternion.clone()
            }
        }

    private val _quaternion = Quaternion.zero.toFloatArray()
    private val _angularRate = FloatArray(3)

    private val NS2S = 1.0f / 1000000000.0f

    override val hasValidReading: Boolean
        get() = _hasReading

    private var _hasReading = false
    private var lastTime = 0L

    private val deltaRotationVector = FloatArray(4)

    private val lock = Object()

    override fun handleSensorEvent(event: SensorEvent) {
        if (event.values.size < 3) {
            return
        }

        if (lastTime == 0L) {
            lastTime = event.timestamp
            return
        }
        val dt = (event.timestamp - lastTime) * NS2S
        lastTime = event.timestamp


        var axisX = -event.values[1]
        var axisY = -event.values[0]
        var axisZ = -event.values[2]

        val omegaMagnitude = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)

        if (omegaMagnitude > threshold) {
            axisX /= omegaMagnitude
            axisY /= omegaMagnitude
            axisZ /= omegaMagnitude
        }

        val thetaOverTwo = omegaMagnitude * dt / 2.0f
        val sinThetaOverTwo = sin(thetaOverTwo)
        val cosThetaOverTwo = cos(thetaOverTwo)

        synchronized(lock) {
            _angularRate[0] = axisX.toDegrees() * dt
            _angularRate[1] = axisY.toDegrees() * dt
            _angularRate[2] = axisZ.toDegrees() * dt
            deltaRotationVector[0] = sinThetaOverTwo * axisX
            deltaRotationVector[1] = sinThetaOverTwo * axisY
            deltaRotationVector[2] = sinThetaOverTwo * axisZ
            deltaRotationVector[3] = cosThetaOverTwo
            QuaternionMath.multiply(_quaternion, deltaRotationVector, _quaternion)
            QuaternionMath.normalize(_quaternion, _quaternion)
        }

        _hasReading = true
    }
}