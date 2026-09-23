package com.kylecorry.andromeda.sense.v2.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.kylecorry.andromeda.sense.v2.BaseSensorManagerSensorFlow
import com.kylecorry.andromeda.sense.v2.SensorAccuracy
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig
import java.time.Instant

class StepDetectorSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseSensorManagerSensorFlow<StepDetectorReading>(context, Sensor.TYPE_STEP_DETECTOR, config) {
    override fun map(event: SensorEvent): StepDetectorReading {
        return StepDetectorReading(
            eventTime = Instant.now(),
            eventTimeElapsedNanos = event.timestamp,
            stepDetected = event.values[0] > 0f,
            accuracy = SensorAccuracy.fromAndroid(event.accuracy)
        )
    }
}
