package com.kylecorry.andromeda.signal

internal interface CellSignalDistanceCalculator {
    fun getTimingAdvanceDistance(timingAdvance: Int): Float
    fun getTimingAdvanceDistanceError(timingAdvance: Int): Float
}