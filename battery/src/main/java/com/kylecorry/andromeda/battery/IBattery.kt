package com.kylecorry.andromeda.battery

import com.kylecorry.andromeda.core.sensors.ISensor
import com.kylecorry.andromeda.core.sensors.IThermometer

interface IBattery : ISensor, IThermometer {
    val percent: Float
    val capacity: Float
    val maxCapacity: Float
    val health: BatteryHealth
    val voltage: Float
    val current: Float
    val chargingMethod: BatteryChargingMethod
    val chargingStatus: BatteryChargingStatus
}