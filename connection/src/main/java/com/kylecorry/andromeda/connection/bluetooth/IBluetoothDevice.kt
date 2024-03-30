package com.kylecorry.andromeda.connection.bluetooth

import android.annotation.SuppressLint
import java.io.InputStream
import java.io.OutputStream

interface IBluetoothDevice {
    val address: String
    val name: String

    // TODO: Let the consumer know it requires a permission
    @SuppressLint("MissingPermission")
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean

    // Reading
    fun read(): Int
    fun readUntil(predicate: (char: Char) -> Boolean): String
    fun readUntil(stop: Char): String
    fun readLine(): String

    // Writing
    fun write(str: String)
    fun write(bytes: ByteArray)
    fun write(byte: Byte)

    fun getInputStream(): InputStream?
    fun getOutputStream(): OutputStream?
}