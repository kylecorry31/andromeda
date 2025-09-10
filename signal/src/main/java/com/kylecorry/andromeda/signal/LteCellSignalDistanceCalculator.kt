package com.kylecorry.andromeda.signal

internal class LteCellSignalDistanceCalculator : CellSignalDistanceCalculator {
    private val binDistance = 78.125f
    override fun getTimingAdvanceDistance(timingAdvance: Int): Float {
        // https://5g-tools.com/4g-lte-timing-advance-distance-calculator/
        return timingAdvance * binDistance
    }

    override fun getTimingAdvanceDistanceError(timingAdvance: Int): Float {
        return binDistance / 2
    }
}