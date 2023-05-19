package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor

class RotationSensor(context: Context, useTrueNorth: Boolean) :
    BaseWorldRotationSensor(context, useTrueNorth, Sensor.TYPE_ROTATION_VECTOR)