package com.kylecorry.andromeda.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.kylecorry.andromeda.permissions.PermissionService

class BluetoothService(private val context: Context) {

    private val adapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val permission by lazy { PermissionService(context) }

    val isEnabled: Boolean
        @SuppressLint("MissingPermission")
        get(){
            return if (permission.canUseBluetooth()) adapter.isEnabled else false
        }


    val devices: List<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get(){
            return if (permission.canUseBluetooth()) adapter.bondedDevices.toList() else listOf()
        }

    fun getDevice(address: String): BluetoothDevice? {
        return try {
            adapter.getRemoteDevice(address)
        } catch (e: Exception){
            null
        }
    }

}