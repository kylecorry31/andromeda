package com.kylecorry.andromeda.sense.v2.temperature

import android.content.Context
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.units.Temperature
import java.time.Instant

abstract class BaseTemperatureSensorFlow(
    context: Context,
    sensorType: Int,
    config: SensorManagerSensorFlowConfig
) : BaseSensorManagerSensorFlow<TemperatureReading>(context, sensorType, config) {
    final override fun map(event: SensorEvent): TemperatureReading {
        return TemperatureReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            temperature = Temperature.celsius(event.values[0]),
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
