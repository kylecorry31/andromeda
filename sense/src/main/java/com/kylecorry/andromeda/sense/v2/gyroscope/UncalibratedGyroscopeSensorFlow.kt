package com.kylecorry.andromeda.sense.v2.gyroscope

import android.content.Context
import android.hardware.Sensor
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

class UncalibratedGyroscopeSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseGyroscopeSensorFlow(context, Sensor.TYPE_GYROSCOPE_UNCALIBRATED, config)
