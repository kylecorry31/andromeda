package com.kylecorry.andromeda.signal

import com.kylecorry.sol.math.SolMath

internal class NrCellSignalDistanceCalculator(private val subcarrierSpacingExponent: Int = 0) :
    CellSignalDistanceCalculator {

    private val binDistance = 78.125f / (SolMath.power(2, subcarrierSpacingExponent))

    override fun getTimingAdvanceDistance(timingAdvance: Int): Float {
        // This will suffice for now (not exact): https://5g-tools.com/5g-nr-timing-advance-ta-distance-calculator/
        return timingAdvance * binDistance
    }

    override fun getTimingAdvanceDistanceError(timingAdvance: Int): Float {
        return binDistance / 2
    }
}