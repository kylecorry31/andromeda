package com.kylecorry.andromeda.connection.nearby

import java.io.InputStream
import java.io.OutputStream

interface NearbyDevice {
    // Connection
    suspend fun connect()
    fun disconnect()

    // TODO: Add a connection status listener/flow
    val connectionStatus: ConnectionStatus

    // TODO: Maybe only expose read/write functions to prevent misuse
    // IO
    val inputStream: InputStream?
    val outputStream: OutputStream?

    // Details
    val name: String
    val identifier: String
    val type: NearbyDeviceType

    // TODO: Add a signal strength listener/flow
    val rssi: Int?
}