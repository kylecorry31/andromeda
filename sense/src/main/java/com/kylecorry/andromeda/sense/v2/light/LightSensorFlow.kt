package com.kylecorry.andromeda.sense.v2.light

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import java.time.Instant

class LightSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<LightReading>(context, Sensor.TYPE_LIGHT, config) {
    override fun map(event: SensorEvent): LightReading {
        return LightReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            illuminance = event.values[0],
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
