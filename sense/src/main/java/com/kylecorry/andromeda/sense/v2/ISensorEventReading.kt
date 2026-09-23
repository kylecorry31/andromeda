package com.kylecorry.andromeda.sense.v2

interface ISensorEventReading : ISensorReading {
    val accuracy: SensorAccuracy
}
