package com.kylecorry.andromeda.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.tryOrNothing
import java.io.InputStream
import java.io.OutputStream

abstract class BaseBluetoothDevice(private val context: Context, override val address: String) :
    IBluetoothDevice {

    private val adapter by lazy { context.getSystemService<BluetoothManager>()?.adapter }
    private val device by lazy { adapter?.getRemoteDevice(address) }
    private var socket: BluetoothSocket? = null

    private var input: InputStream? = null
    private var output: OutputStream? = null

    override val name: String
        @SuppressLint("MissingPermission")
        get() = device?.name ?: ""

    // TODO: Let the consumer know it requires a permission
    @SuppressLint("MissingPermission")
    override fun connect() {
        if (isConnected() || adapter?.isEnabled != true || device == null) {
            return
        }
        adapter?.cancelDiscovery()
        socket = getSocket(device!!)
        try {
            socket?.connect()
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
        val builder = StringBuilder()
        var b: Int
        while ((input.read().also { b = it }) > -1) {
            val char = b.toChar()
            if (predicate(char)) break
            builder.append(char)
        }

        return builder.toString()
    }

    override fun readUntil(stop: Char): String {
        if (!isConnected()) return ""
        return readUntil { it == stop }
    }

    override fun readLine(): String {
        if (!isConnected()) return ""
        return readUntil { it == '\n' }
    }

    // Writing
    override fun write(str: String) {
        if (!isConnected()) return
        output?.write(str.toByteArray())
        output?.flush()
    }

    override fun write(bytes: ByteArray) {
        if (!isConnected()) return
        output?.write(bytes)
        output?.flush()
    }

    override fun write(byte: Byte) {
        if (!isConnected()) return
        output?.write(byte.toInt())
        output?.flush()
    }

    override fun getInputStream(): InputStream? {
        return input
    }

    override fun getOutputStream(): OutputStream? {
        return output
    }

    protected abstract fun getSocket(device: BluetoothDevice): BluetoothSocket

}