package com.kylecorry.andromeda.sense.v2.gyroscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.math.Quaternion
import java.time.Instant
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GyroscopeOrientationSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<GyroscopeOrientationReading>(context, Sensor.TYPE_GYROSCOPE, config) {
    private var orientation = Quaternion.zero
    private var lastTimestamp: Long? = null

    override fun onStartListening() {
        orientation = Quaternion.zero
        lastTimestamp = null
    }

    override fun map(event: SensorEvent): GyroscopeOrientationReading {
        val previousTimestamp = lastTimestamp
        lastTimestamp = event.timestamp

        if (previousTimestamp != null && event.timestamp > previousTimestamp) {
            val deltaSeconds = (event.timestamp - previousTimestamp) / 1_000_000_000f
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt(x * x + y * y + z * z)

            if (magnitude > 0f) {
                val halfAngle = magnitude * deltaSeconds / 2f
                val scale = sin(halfAngle) / magnitude
                val delta = Quaternion(
                    x * scale,
                    y * scale,
                    z * scale,
                    cos(halfAngle)
                )
                orientation = (orientation * delta).normalize()
            }
        }

        return GyroscopeOrientationReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            quaternion = orientation,
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
