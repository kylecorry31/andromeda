package com.kylecorry.andromeda.sense.v2.readings

import com.kylecorry.andromeda.sense.v2.SensorAccuracy

interface SensorAccuracyReading : EventTimeReading {
    val accuracy: SensorAccuracy
}
