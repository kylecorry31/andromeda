package com.kylecorry.andromeda.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.time.Timer
import com.kylecorry.andromeda.permissions.Permissions

/**
 * For SDK 30 or older: Manifest.permission.BLUETOOTH_ADMIN  (non-protected permission)
 * For SDK 31 or newer: Manifest.permission.BLUETOOTH_SCAN (protected permission)
 * For all devices: Manifest.permission.ACCESS_FINE_LOCATION or neverForLocation on the associated bluetooth permission
 *
 */
class BluetoothLEScanner(private val context: Context) : AbstractSensor() {

    val devices = mutableListOf<BluetoothDevice>()
    private var isDoneScanning = false
    private val adapter by lazy { BluetoothService(context).adapter }
    private val receiver = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device = result?.device
            device?.let {
                val existing = devices.firstOrNull { it.address == device.address }
                if (existing != null) {
                    devices.remove(existing)
                }
                devices.add(device)
                notifyListeners()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val timeout = Timer {
        if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)) {
            adapter?.bluetoothLeScanner?.stopScan(receiver)
            isDoneScanning = true
        }
    }

    override val hasValidReading: Boolean
        get() = isDoneScanning

    @SuppressLint("MissingPermission")
    override fun startImpl() {
        isDoneScanning = false
        if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)) {
            timeout.once(1000 * 12)
            adapter?.bluetoothLeScanner?.startScan(receiver)
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopImpl() {
        isDoneScanning = false
        if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)) {
            timeout.stop()
            adapter?.bluetoothLeScanner?.stopScan(receiver)
        }
    }
}