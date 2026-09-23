package com.kylecorry.andromeda.sense.v2.accelerometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

class LinearAccelerationSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(
        sensorDelayUs = SensorManager.SENSOR_DELAY_GAME
    )
) : BaseAccelerometerSensorFlow(
    context,
    Sensor.TYPE_LINEAR_ACCELERATION,
    config
)
