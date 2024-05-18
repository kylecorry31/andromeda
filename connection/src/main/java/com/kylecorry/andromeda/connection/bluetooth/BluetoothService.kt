package com.kylecorry.andromeda.connection.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.permissions.NoPermissionException
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.luna.coroutines.onIO
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.*

class BluetoothService(private val context: Context) {

    val adapter by lazy { context.getSystemService<BluetoothManager>()?.adapter }

    val isEnabled: Boolean
        get() {
            Permissions.requireLegacyBluetooth(context)
            return adapter?.isEnabled == true
        }


    val bondedDevices: List<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get() {
            Permissions.requireBluetoothConnect(context, requireLegacyPermission = true)
            return adapter?.bondedDevices?.toList() ?: emptyList()
        }

    fun getDevice(address: String): BluetoothDevice? {
        return try {
            adapter?.getRemoteDevice(address)
        } catch (e: Exception) {
            null
        }
    }

    fun getSecureDevice(address: String, uuid: UUID = DEFAULT_SERIAL_UUID): IBluetoothDevice {
        return SecureBluetoothDevice(context, address, uuid)
    }

    fun getInsecureDevice(address: String, uuid: UUID = DEFAULT_SERIAL_UUID): IBluetoothDevice {
        return InsecureBluetoothDevice(context, address, uuid)
    }

    fun getGattDevice(address: String): BluetoothGattDevice {
        return BluetoothGattDevice(context, address)
    }

    companion object {
        val DEFAULT_SERIAL_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

}