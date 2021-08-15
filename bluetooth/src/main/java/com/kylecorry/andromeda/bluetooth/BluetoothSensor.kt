package com.kylecorry.andromeda.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import java.io.*
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread

// TODO: Improve the use of threads here
class BluetoothSensor(
    private val context: Context,
    private val address: String,
    private val messageHistoryLength: Int
) :
    AbstractSensor(), IBluetoothSensor {

    private val bluetoothService by lazy { BluetoothService(context) }
    private val device by lazy { bluetoothService.getDevice(address) }

    override val hasValidReading: Boolean
        get() = messages.isNotEmpty()

    override fun startImpl() {
        connect()
    }

    override fun stopImpl() {
        disconnect()
    }

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    override val messages: List<BluetoothMessage>
        get() = _messages

    override val isConnected: Boolean
        get() = socket?.isConnected == true

    private var _messages = listOf<BluetoothMessage>()
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("MissingPermission")
    private fun connect() {
        thread {
            try {
                if (bluetoothService.isEnabled) {
                    socket =
                        device?.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                }
                socket?.connect()
                input = socket?.inputStream
                output = socket?.outputStream
            } catch (e: IOException) {
                socket?.close()
                e.printStackTrace()
            }

            if (isConnected) {
                startInputListener()
            }

            handler.post {
                notifyListeners()
            }
        }
    }

    private fun disconnect() {
        if (isConnected) {
            input?.close()
            output?.close()
            socket?.close()
        }

        socket = null
        input = null
        output = null

        notifyListeners()
    }

    override fun write(data: String): Boolean {
        if (!isConnected) return false
        val bytes = data.toByteArray()
        try {
            output?.write(bytes)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun startInputListener() {
        thread {
            val inputStream = input!!

            val reader = BufferedReader(InputStreamReader(inputStream))

            while (isConnected) {
                synchronized(inputStream) {
                    try {
                        val recv = reader.readLine()
                        val message = BluetoothMessage(recv, Instant.now())
                        val lastMessages = _messages.toMutableList()
                        lastMessages.add(message)
                        while (lastMessages.size > messageHistoryLength) {
                            lastMessages.removeAt(0)
                        }
                        _messages = lastMessages
                        handler.post { notifyListeners() }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            handler.post { notifyListeners() }

        }
    }

}