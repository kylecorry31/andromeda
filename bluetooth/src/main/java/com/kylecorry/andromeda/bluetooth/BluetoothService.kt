package com.kylecorry.andromeda.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

class BluetoothService {

    private val adapter by lazy { BluetoothAdapter.getDefaultAdapter() }

    val isEnabled: Boolean
        get(){
            return adapter.isEnabled
        }

    val devices: List<BluetoothDevice>
        get(){
            return adapter.bondedDevices.toList()
        }

    fun getDevice(address: String): BluetoothDevice? {
        return try {
            adapter.getRemoteDevice(address)
        } catch (e: Exception){
            null
        }
    }

}