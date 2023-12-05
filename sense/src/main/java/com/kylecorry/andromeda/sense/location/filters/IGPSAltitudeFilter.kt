package com.kylecorry.andromeda.sense.location.filters

interface IGPSAltitudeFilter {
    val altitude: Float
    val accuracy: Float?
    val hasValidReading: Boolean
    fun update(altitude: Float, accuracy: Float?)
    fun reset()
}