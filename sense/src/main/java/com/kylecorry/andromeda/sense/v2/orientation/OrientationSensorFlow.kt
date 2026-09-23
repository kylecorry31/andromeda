package com.kylecorry.andromeda.sense.v2.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import com.kylecorry.sol.math.Euler
import java.time.Instant

@Suppress("DEPRECATION")
class OrientationSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<OrientationReading>(context, Sensor.TYPE_ORIENTATION, config) {
    override fun map(event: SensorEvent): OrientationReading {
        return OrientationReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            orientation = Euler(
                roll = event.values[2],
                pitch = event.values[1],
                yaw = event.values[0]
            ),
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
