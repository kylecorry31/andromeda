package com.kylecorry.andromeda.sense.v2.rotation

import android.content.Context
import android.hardware.Sensor
import com.kylecorry.andromeda.sense.v2.SensorManagerSensorFlowConfig

class RotationVectorSensorFlow(
    context: Context,
    config: SensorManagerSensorFlowConfig = SensorManagerSensorFlowConfig()
) : BaseRotationVectorSensorFlow(context, Sensor.TYPE_ROTATION_VECTOR, config, true)
