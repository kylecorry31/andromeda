package com.kylecorry.andromeda.connection.nearby

import com.kylecorry.andromeda.connection.bluetooth.IBluetoothDevice
import com.kylecorry.luna.coroutines.onIO
import java.io.InputStream
import java.io.OutputStream

class BluetoothNearbyDevice(private val device: IBluetoothDevice) : NearbyDevice {

    private var isConnecting = false

    override suspend fun connect() = onIO {
        if (isConnecting || device.isConnected()) {
            return@onIO
        }
        isConnecting = true
        device.connect()
        isConnecting = false
    }

    override fun disconnect() {
        if (!isConnecting && !device.isConnected()) {
            return
        }
        isConnecting = false
        device.disconnect()
    }

    override val connectionStatus: ConnectionStatus
        get() = if (isConnecting) {
            ConnectionStatus.Connecting
        } else if (device.isConnected()) {
            ConnectionStatus.Connected
        } else {
            ConnectionStatus.Disconnected
        }

    override val inputStream: InputStream?
        get() = device.getInputStream()

    override val outputStream: OutputStream?
        get() = device.getOutputStream()

    override val name: String
        get() = device.name

    override val identifier: String
        get() = device.address

    override val type: NearbyDeviceType
        get() = NearbyDeviceType.Bluetooth

    override val rssi: Int?
        get() = null
}