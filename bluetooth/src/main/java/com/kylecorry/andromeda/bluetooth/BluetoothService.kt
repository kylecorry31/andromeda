package com.kylecorry.andromeda.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.kylecorry.andromeda.permissions.Permissions

class BluetoothService(private val context: Context) {

    private val adapter by lazy { BluetoothAdapter.getDefaultAdapter() }

    val isEnabled: Boolean
        @SuppressLint("MissingPermission")
        get(){
            return if (Permissions.canUseBluetooth(context)) adapter.isEnabled else false
        }


    val devices: List<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get(){
            return if (Permissions.canUseBluetooth(context)) adapter.bondedDevices.toList() else listOf()
        }

    fun getDevice(address: String): BluetoothDevice? {
        return try {
            adapter.getRemoteDevice(address)
        } catch (e: Exception){
            null
        }
    }

}