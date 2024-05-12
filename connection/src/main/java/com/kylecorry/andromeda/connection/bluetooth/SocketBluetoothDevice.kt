package com.kylecorry.andromeda.connection.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context

class SocketBluetoothDevice(
    context: Context,
    address: String,
    private val socket: BluetoothSocket
) : BaseBluetoothDevice(context, address) {
    override fun getSocket(device: BluetoothDevice): BluetoothSocket {
        return socket
    }
}