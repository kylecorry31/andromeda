package com.kylecorry.andromeda.sense.v2.accelerometer

import android.content.Context
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.math.Vector3
import java.time.Instant

abstract class BaseAccelerometerSensorFlow(
    context: Context,
    sensorType: Int,
    config: SensorManagerSensorFlowConfig
) : BaseSensorManagerSensorFlow<AccelerometerReading>(context, sensorType, config) {
    override fun map(event: SensorEvent): AccelerometerReading {
        return AccelerometerReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            acceleration = Vector3(event.values[0], event.values[1], event.values[2]),
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
