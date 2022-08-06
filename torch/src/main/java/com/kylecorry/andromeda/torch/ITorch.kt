package com.kylecorry.andromeda.torch

interface ITorch {

    /**
     * The number of brightness levels supported by the torch
     */
    val brightnessLevels: Int

    /**
     * Turn the torch on
     */
    fun on()

    /**
     * Turn on the torch at the set brightness
     * @param brightness the brightness in [0, 1]
     */
    fun on(brightness: Float)

    /**
     * Turn the torch off
     */
    fun off()

    /**
     * Determines if the device has a torch
     * @return true if a torch exists, false otherwise
     */
    fun isAvailable(): Boolean

    /**
     * Determines if the torch is dimmable
     */
    fun isDimmable(): Boolean
}