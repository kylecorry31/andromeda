package com.kylecorry.andromeda.bluetooth

interface IBluetoothSensor {
    val messages: List<BluetoothMessage>
    val isConnected: Boolean

    // TODO: Make this a suspend function
    fun write(data: String): Boolean
}