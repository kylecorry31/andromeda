package com.kylecorry.andromeda.location

import com.kylecorry.andromeda.core.sensors.IAltimeter
import com.kylecorry.andromeda.core.sensors.IClock
import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.andromeda.core.sensors.ISpeedometer
import com.kylecorry.sol.units.Coordinate

interface IGPS: ISensor, IAltimeter, IClock, ISpeedometer {
    val location: Coordinate
    val verticalAccuracy: Float?
    val horizontalAccuracy: Float?
    val satellites: Int
    val mslAltitude: Float?
}