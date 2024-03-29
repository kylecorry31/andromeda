package com.kylecorry.andromeda.sense.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.kylecorry.sol.math.Quaternion
import com.kylecorry.sol.math.QuaternionMath
import com.kylecorry.sol.units.Bearing
import com.kylecorry.andromeda.sense.BaseSensor
import com.kylecorry.andromeda.sense.compass.ICompass

class GeomagneticRotationSensor(
    context: Context, sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME
) : BaseRotationSensor(
    context, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, sensorDelay
)