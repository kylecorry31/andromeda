package com.kylecorry.andromeda.sense.orientation.filter

interface OrientationSensorFilter {
    fun filter(quaternion: FloatArray, out: FloatArray)
    fun reset(value: FloatArray)
}