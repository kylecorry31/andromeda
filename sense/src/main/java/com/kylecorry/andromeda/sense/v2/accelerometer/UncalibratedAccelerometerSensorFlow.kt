package com.kylecorry.andromeda.sense.v2.accelerometer

import android.content.Context
import android.hardware.Sensor
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

class UncalibratedAccelerometerSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseAccelerometerSensorFlow(
    context,
    Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
    config
)
