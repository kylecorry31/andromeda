package com.kylecorry.andromeda.sense.location.filters

import com.kylecorry.sol.math.RingBuffer
import com.kylecorry.sol.math.SolMath.positive
import com.kylecorry.sol.math.SolMath.real
import com.kylecorry.sol.math.statistics.GaussianDistribution
import com.kylecorry.sol.math.statistics.Statistics

class GPSGaussianAltitudeFilter(samples: Int = 4) : IGPSAltitudeFilter {

    private val buffer = RingBuffer<GaussianDistribution>(samples)

    private var lastDistribution: GaussianDistribution? = null

    private val defaultVariance = 10f

    override var altitude: Float = 0f
        private set

    override var accuracy: Float? = null
        private set

    override val hasValidReading: Boolean
        get() = buffer.isFull()

    override fun update(altitude: Float, accuracy: Float?) {
        // Always populate the variance
        val variance = accuracy
            ?.real(defaultVariance)
            ?.positive(defaultVariance)
            ?: defaultVariance

        val distribution = GaussianDistribution(
            altitude.real(0f),
            variance
        )

        // A new elevation reading was not received
        if (distribution == lastDistribution) {
            return
        }

        lastDistribution = distribution

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