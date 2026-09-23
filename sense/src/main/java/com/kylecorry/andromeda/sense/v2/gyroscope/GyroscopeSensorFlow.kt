package com.kylecorry.andromeda.sense.v2.gyroscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

class GyroscopeSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(
        sensorDelayUs = SensorManager.SENSOR_DELAY_GAME
    )
) : BaseGyroscopeSensorFlow(context, Sensor.TYPE_GYROSCOPE, config)
