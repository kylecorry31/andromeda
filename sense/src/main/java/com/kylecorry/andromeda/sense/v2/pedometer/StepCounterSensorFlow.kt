package com.kylecorry.andromeda.sense.v2.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import java.time.Instant

class StepCounterSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<StepCounterReading>(context, Sensor.TYPE_STEP_COUNTER, config) {
    override fun map(event: SensorEvent): StepCounterReading {
        return StepCounterReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            steps = event.values[0].toInt(),
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
