package com.kylecorry.andromeda.connection

import com.kylecorry.andromeda.core.sensors.ISensor

interface NearbyDeviceScanner : ISensor {
    val devices: List<NearbyDevice>
}