package com.kylecorry.andromeda.sense.v2.gyroscope

import android.content.Context
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.math.Vector3
import java.time.Instant

abstract class BaseGyroscopeSensorFlow(
    context: Context,
    sensorType: Int,
    config: SensorManagerSensorFlowConfig
) : BaseSensorManagerSensorFlow<GyroscopeReading>(context, sensorType, config) {
    final override fun map(event: SensorEvent): GyroscopeReading {
        return GyroscopeReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            angularRate = Vector3(event.values[0], event.values[1], event.values[2]),
            bias = if (event.values.size >= 6) {
                Vector3(event.values[3], event.values[4], event.values[5])
            } else {
                null
            },
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
