package com.kylecorry.andromeda.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.permissions.Permissions

/**
 * For SDK 30 or older: Manifest.permission.BLUETOOTH_ADMIN  (non-protected permission)
 * For SDK 31 or newer: Manifest.permission.BLUETOOTH_SCAN (protected permission)
 * For all devices: Manifest.permission.ACCESS_FINE_LOCATION or neverForLocation on the associated bluetooth permission
 *
 */
class BluetoothScanner(private val context: Context) : AbstractSensor() {

    val devices = mutableListOf<BluetoothDevice>()

    private var isDoneScanning = false

    private val adapter by lazy { BluetoothService(context).adapter }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val existing = devices.firstOrNull { it.address == device.address }
                        if (existing != null) {
                            devices.remove(existing)
                        }
                        devices.add(device)
                        notifyListeners()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    isDoneScanning = true
                    notifyListeners()
                }
            }
        }
    }

    override val hasValidReading: Boolean
        get() = isDoneScanning

    @SuppressLint("MissingPermission")
    override fun startImpl() {
        isDoneScanning = false
        if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            context.registerReceiver(receiver, filter)
            adapter?.startDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopImpl() {
        isDoneScanning = false
        if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)) {
            adapter?.cancelDiscovery()
            context.unregisterReceiver(receiver)
        }
    }
}