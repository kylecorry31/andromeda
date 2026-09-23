package com.kylecorry.andromeda.sense.v2.hygrometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import java.time.Instant

class HygrometerSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<HygrometerReading>(context, Sensor.TYPE_RELATIVE_HUMIDITY, config) {
    override fun map(event: SensorEvent): HygrometerReading {
        return HygrometerReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            humidity = event.values[0],
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
