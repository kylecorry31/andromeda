package com.kylecorry.andromeda.sense.clinometer

import com.kylecorry.andromeda.core.sensors.ISensor

interface IClinometer : ISensor {
    /**
     * The incline between -90 and 90
     */
    val incline: Float
}