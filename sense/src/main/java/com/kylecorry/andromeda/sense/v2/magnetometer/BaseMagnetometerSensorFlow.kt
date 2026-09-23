package com.kylecorry.andromeda.sense.v2.magnetometer

import android.content.Context
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.math.Vector3
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
            magneticField = Vector3(event.values[0], event.values[1], event.values[2]),
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
