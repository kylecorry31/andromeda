package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

class RotationSensor(
    context: Context,
    useTrueNorth: Boolean,
    sensorDelay: Int = SensorManager.SENSOR_DELAY_FASTEST
) :
    BaseWorldRotationSensor(context, useTrueNorth, Sensor.TYPE_ROTATION_VECTOR, sensorDelay)