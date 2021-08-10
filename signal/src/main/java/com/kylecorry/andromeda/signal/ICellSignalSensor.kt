package com.kylecorry.andromeda.signal

import com.kylecorry.andromeda.core.sensors.ISensor

interface ICellSignalSensor: ISensor {
    val signals: List<CellSignal>
}