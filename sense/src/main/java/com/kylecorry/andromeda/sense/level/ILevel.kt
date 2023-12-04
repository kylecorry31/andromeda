package com.kylecorry.andromeda.sense.level

import com.kylecorry.andromeda.core.sensors.ISensor

interface ILevel : ISensor {
    /**
     * The X level in degrees
     */
    val x: Float

    /**
     * The Y level in degrees
     */
    val y: Float
}