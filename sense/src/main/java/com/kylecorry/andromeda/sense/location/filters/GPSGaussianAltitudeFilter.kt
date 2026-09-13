package com.kylecorry.andromeda.sense.location.filters

import com.kylecorry.sol.math.MathExtensions.positive
import com.kylecorry.sol.math.MathExtensions.real
import com.kylecorry.sol.math.RingBuffer
import com.kylecorry.sol.math.statistics.GaussianDistribution
import com.kylecorry.sol.math.statistics.Statistics

class GPSGaussianAltitudeFilter(samples: Int = 4) : IGPSAltitudeFilter {

    private val buffer = RingBuffer<GaussianDistribution>(samples)

    private var lastFixTimeElapsedNanos: Long? = null

    private val defaultVariance = 10f

    override var altitude: Float = 0f
        private set

    override var accuracy: Float? = null
        private set

    override val hasValidReading: Boolean
        get() = buffer.isFull()

    override fun update(altitude: Float, accuracy: Float?, fixTimeElapsedNanos: Long) {
        if (fixTimeElapsedNanos == lastFixTimeElapsedNanos) {
            return
        }

        lastFixTimeElapsedNanos = fixTimeElapsedNanos

        // Always populate the variance
        val variance = accuracy
            ?.real(defaultVariance)
            ?.positive(defaultVariance)
            ?: defaultVariance

        val distribution = GaussianDistribution(
            altitude.real(0f),
            variance
        )

        buffer.add(distribution)
        val calculated = Statistics.joint(buffer.toList())
        if (calculated != null) {
            this.altitude = calculated.mean.real(0f)
            this.accuracy =
                calculated.standardDeviation.real(defaultVariance).positive(defaultVariance)
        }
    }

    override fun reset() {
        buffer.clear()
    }
}
