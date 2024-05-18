package com.kylecorry.andromeda.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.luna.coroutines.onIO
import java.util.UUID

class BluetoothListener(private val context: Context) {

    private val bluetooth = BluetoothService(context)
    private var serverSocket: BluetoothServerSocket? = null

    @SuppressLint("MissingPermission")
    suspend fun listen(name: String, uuid: UUID, secure: Boolean = true): IBluetoothDevice? = onIO {
        Permissions.requireBluetoothConnect(context, requireLegacyPermission = true)
        serverSocket?.close()
        serverSocket = if (secure) {
            bluetooth.adapter?.listenUsingRfcommWithServiceRecord(name, uuid)
        } else {
            bluetooth.adapter?.listenUsingInsecureRfcommWithServiceRecord(name, uuid)
        }
        val socket = serverSocket?.use { socket ->
            socket.accept()
        }
        serverSocket = null
        socket?.let {
            SocketBluetoothDevice(context, it.remoteDevice.address, it) { device ->
                if (secure) {
                    device.createRfcommSocketToServiceRecord(uuid)
                } else {
                    device.createInsecureRfcommSocketToServiceRecord(uuid)
                }
            }
        }
    }

    fun close() {
        serverSocket?.close()
        serverSocket = null
    }

}