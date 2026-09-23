package com.kylecorry.andromeda.sense.v2.rotation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

class GameRotationVectorSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig(
        sensorDelayUs = SensorManager.SENSOR_DELAY_GAME
    )
) : BaseRotationVectorSensorFlow(context, Sensor.TYPE_GAME_ROTATION_VECTOR, config, false)
