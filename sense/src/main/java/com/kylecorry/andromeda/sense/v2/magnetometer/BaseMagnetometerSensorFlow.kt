package com.kylecorry.andromeda.sense.v2.magnetometer

import android.content.Context
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import java.time.Instant

abstract class BaseMagnetometerSensorFlow(
    context: Context,
    sensorType: Int,
    config: SensorManagerSensorFlowConfig
) : BaseSensorManagerSensorFlow<MagnetometerReading>(context, sensorType, config) {
    final override fun map(event: SensorEvent): MagnetometerReading {
        return MagnetometerReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            x = event.values[0],
            y = event.values[1],
            z = event.values[2],
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
