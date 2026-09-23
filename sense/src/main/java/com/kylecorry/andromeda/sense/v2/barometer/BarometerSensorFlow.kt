package com.kylecorry.andromeda.sense.v2.barometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.units.Pressure
import java.time.Instant

class BarometerSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<BarometerReading>(context, Sensor.TYPE_PRESSURE, config) {
    override fun map(event: SensorEvent): BarometerReading {
        return BarometerReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            pressure = Pressure.hpa(event.values[0]),
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
