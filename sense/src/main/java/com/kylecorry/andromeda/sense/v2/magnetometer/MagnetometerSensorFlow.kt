package com.kylecorry.andromeda.sense.v2.magnetometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

class MagnetometerSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(
        sensorDelayUs = SensorManager.SENSOR_DELAY_GAME
    )
) :
    BaseMagnetometerSensorFlow(
        context,
        Sensor.TYPE_MAGNETIC_FIELD,
        config
    )
