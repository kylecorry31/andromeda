package com.kylecorry.andromeda.sense.location.filters

import com.kylecorry.sol.math.filters.IFilter
import com.kylecorry.sol.math.filters.MedianFilter

class GPSAltitudeFilter(
    private val filterProvider: (initialValue: Float) -> IFilter,
    private val minimumSamples: Int
) : IGPSAltitudeFilter {
    override var altitude: Float = 0f
        private set

    override var accuracy: Float? = null
        private set

    private var samples = 0

    override var hasValidReading: Boolean = false

    private var filter: IFilter? = null

    override fun update(altitude: Float, accuracy: Float?) {
        val filter = filter ?: filterProvider(altitude)
        this.filter = filter
        this.altitude = filter.filter(altitude)
        this.accuracy = accuracy
        samples++
        hasValidReading = samples >= minimumSamples
    }

    override fun reset() {
        filter = null
        altitude = 0f
        accuracy = null
        samples = 0
        hasValidReading = false
    }
}