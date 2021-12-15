package com.kylecorry.andromeda.sense.clinometer

import com.kylecorry.andromeda.core.sensors.ISensor

interface IClinometer : ISensor {
    /**
     * The unit angle in degrees. Front (horizon) is 0, above (sky) is 90, back (horizon) is 180, and below (ground) is 270.
     */
    val angle: Float

    /**
     * The incline between -90 and 90
     */
    val incline: Float
}