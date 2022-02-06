package com.kylecorry.andromeda.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.permissions.Permissions
import java.util.*

class BluetoothService(private val context: Context) {

    private val adapter by lazy { context.getSystemService<BluetoothManager>()?.adapter }

    val isEnabled: Boolean
        @SuppressLint("MissingPermission")
        get(){
            return if (Permissions.canUseBluetooth(context) && adapter != null) adapter!!.isEnabled else false
        }


    val devices: List<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get(){
            return if (Permissions.canUseBluetooth(context) && adapter != null) adapter!!.bondedDevices.toList() else listOf()
        }

    fun getDevice(address: String): BluetoothDevice? {
        return try {
            adapter?.getRemoteDevice(address)
        } catch (e: Exception){
            null
        }
    }

    fun getSecureDevice(address: String, uuid: UUID = DEFAULT_SERIAL_UUID): IBluetoothDevice {
        return SecureBluetoothDevice(context, address, uuid)
    }

    fun getInsecureDevice(address: String, uuid: UUID = DEFAULT_SERIAL_UUID): IBluetoothDevice {
        return InsecureBluetoothDevice(context, address, uuid)
    }

    companion object {
        val DEFAULT_SERIAL_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

}