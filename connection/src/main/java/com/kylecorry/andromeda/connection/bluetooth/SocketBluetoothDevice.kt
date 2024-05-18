package com.kylecorry.andromeda.connection.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context

class SocketBluetoothDevice(
    context: Context,
    address: String,
    private val socket: BluetoothSocket,
    private val fallback: ((device: BluetoothDevice) -> BluetoothSocket)? = null
) :
    BaseBluetoothDevice(
        context,
        address
    ) {
    override fun getSocket(device: BluetoothDevice): BluetoothSocket {
        if (!socket.isConnected && fallback != null) {
            return fallback.invoke(device)
        }
        return socket
    }
}