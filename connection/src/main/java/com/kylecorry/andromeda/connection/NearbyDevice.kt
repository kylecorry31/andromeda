package com.kylecorry.andromeda.connection

import com.kylecorry.andromeda.core.sensors.ISensor

interface NearbyDevice : ISensor {
    val name: String
    val address: String
    val isConnected: Boolean
    val isConnecting: Boolean
    val messages: List<NearbyDeviceMessage>
    var messageHistorySize: Int

    suspend fun send(message: ByteArray)
}