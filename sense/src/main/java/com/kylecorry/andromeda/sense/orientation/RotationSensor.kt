package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

class RotationSensor(
    context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST
) : BaseRotationSensor(context, Sensor.TYPE_ROTATION_VECTOR, sensorDelay)