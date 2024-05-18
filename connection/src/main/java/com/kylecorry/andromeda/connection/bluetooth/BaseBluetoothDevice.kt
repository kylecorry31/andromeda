package com.kylecorry.andromeda.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.io.readUntil
import com.kylecorry.andromeda.core.io.write
import com.kylecorry.andromeda.core.io.writeAll
import com.kylecorry.andromeda.core.tryOrNothing
import com.kylecorry.andromeda.permissions.Permissions
import java.io.InputStream
import java.io.OutputStream

abstract class BaseBluetoothDevice(protected val context: Context, override val address: String) :
    IBluetoothDevice {

    private val adapter by lazy { context.getSystemService<BluetoothManager>()?.adapter }
    private val device by lazy { adapter?.getRemoteDevice(address) }
    private var socket: BluetoothSocket? = null

    private var input: InputStream? = null
    private var output: OutputStream? = null

    override val name: String
        @SuppressLint("MissingPermission")
        get() {
            Permissions.requireBluetoothConnect(context, requireLegacyPermission = true)
            return device?.name ?: ""
        }

    // TODO: Let the consumer know it requires a permission
    @SuppressLint("MissingPermission")
    override fun connect() {
        Permissions.requireLegacyBluetooth(context)
        Permissions.requireBluetoothScan(context, requireLegacyPermission = true)
        Permissions.requireBluetoothConnect(context, requireLegacyPermission = true)

        if (isConnected() || adapter?.isEnabled != true || device == null) {
            return
        }
        adapter?.cancelDiscovery()
        socket = getSocket(device!!)
        try {
            if (socket?.isConnected != true) {
                socket?.connect()
            }
            input = socket?.inputStream
            output = socket?.outputStream
        } catch (e: Exception) {
            disconnect()
        }
    }

    override fun disconnect() {
        tryOrNothing {
            socket?.close()
            socket = null
            input = null
            output = null
        }
    }

    override fun isConnected(): Boolean {
        return socket?.isConnected == true
    }

    // Reading
    override fun read(): Int {
        if (!isConnected()) return -1
        return input?.read() ?: -1
    }

    override fun readUntil(predicate: (char: Char) -> Boolean): String {
        if (!isConnected()) return ""
        val input = input ?: return ""
        return input.readUntil(predicate)
    }

    override fun readUntil(stop: Char): String {
        return readUntil { it == stop }
    }

    override fun readLine(): String {
        return readUntil { it == '\n' }
    }

    // Writing
    override fun write(str: String) {
        if (!isConnected()) return
        output?.write(str)
    }

    override fun write(bytes: ByteArray) {
        if (!isConnected()) return
        output?.writeAll(bytes)
    }

    override fun write(byte: Byte) {
        if (!isConnected()) return
        output?.write(byte)
    }

    override fun getInputStream(): InputStream? {
        return input
    }

    override fun getOutputStream(): OutputStream? {
        return output
    }

    protected abstract fun getSocket(device: BluetoothDevice): BluetoothSocket

}