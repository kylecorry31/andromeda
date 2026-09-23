package com.kylecorry.andromeda.sense.v2

import android.content.Context
import android.hardware.SensorEvent
import java.time.Instant

class SensorManagerSensorFlow(
    context: Context,
    sensorType: Int,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<GenericSensorEventReading>(
    context,
    sensorType,
    config
) {
    override fun map(event: SensorEvent): GenericSensorEventReading {
        return GenericSensorEventReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            values = event.values.clone(),
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
