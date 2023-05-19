package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.andromeda.sense.BaseSensor

class GameRotationSensor(context: Context) : BaseRotationSensor(context, Sensor.TYPE_GAME_ROTATION_VECTOR)