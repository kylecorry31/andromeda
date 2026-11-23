package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Handler

class RotationSensor(
    context: Context,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME,
    maintainStateOnRestart: Boolean = false,
    handler: Handler? = null
) : BaseRotationSensor(
    context,
    Sensor.TYPE_ROTATION_VECTOR,
    sensorDelay,
    maintainStateOnRestart,
    handler
)
