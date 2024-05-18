package com.kylecorry.andromeda.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.kylecorry.andromeda.permissions.Permissions
import java.util.*

class InsecureBluetoothDevice(context: Context, address: String, private val uuid: UUID) : BaseBluetoothDevice(context, address) {
    @SuppressLint("MissingPermission")
    override fun getSocket(device: BluetoothDevice): BluetoothSocket {
        Permissions.requireBluetoothConnect(context, requireLegacyPermission = true)
        return device.createInsecureRfcommSocketToServiceRecord(uuid)
    }
}