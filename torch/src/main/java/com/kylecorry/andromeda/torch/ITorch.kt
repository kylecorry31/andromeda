package com.kylecorry.andromeda.torch

interface ITorch {
    /**
     * Turn the torch on
     */
    fun on()

    /**
     * Turn the torch off
     */
    fun off()

    /**
     * Determines if the device has a torch
     * @return true if a torch exists, false otherwise
     */
    fun isAvailable(): Boolean
}