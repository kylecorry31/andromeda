package com.kylecorry.andromeda.sense.location.filters

class GPSPassThroughAltitudeFilter : IGPSAltitudeFilter {
    override var altitude: Float = 0f
        private set

    override var accuracy: Float? = null
        private set

    override var hasValidReading: Boolean = false
        private set

    override fun update(altitude: Float, accuracy: Float?) {
        this.altitude = altitude
        this.accuracy = accuracy
        hasValidReading = true
    }

    override fun reset() {
        altitude = 0f
        accuracy = null
        hasValidReading = false
    }
}