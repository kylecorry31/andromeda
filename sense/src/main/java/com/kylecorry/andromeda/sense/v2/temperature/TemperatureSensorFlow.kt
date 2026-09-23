package com.kylecorry.andromeda.sense.v2.temperature

import android.content.Context
import android.hardware.Sensor
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

@Suppress("DEPRECATION")
class TemperatureSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseTemperatureSensorFlow(context, Sensor.TYPE_TEMPERATURE, config)
