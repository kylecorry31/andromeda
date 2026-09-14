package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

class GeomagneticRotationSensor(
    context: Context,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME,
    maintainStateOnRestart: Boolean = false
) : BaseRotationSensor(
    context, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, sensorDelay, maintainStateOnRestart
)
