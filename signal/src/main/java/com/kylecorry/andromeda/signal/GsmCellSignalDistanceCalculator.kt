package com.kylecorry.andromeda.signal

internal class GsmCellSignalDistanceCalculator : CellSignalDistanceCalculator {
    private val binDistance = 550f
    override fun getTimingAdvanceDistance(timingAdvance: Int): Float {
        // https://blog.wirelessmoves.com/2017/09/using-the-gsm-timing-advance-to-find-an-lte-base-station.html
        return timingAdvance * binDistance
    }

    override fun getTimingAdvanceDistanceError(timingAdvance: Int): Float {
        return binDistance / 2
    }

}