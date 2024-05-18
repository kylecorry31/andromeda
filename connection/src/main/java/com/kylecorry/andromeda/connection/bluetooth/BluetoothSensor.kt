package com.kylecorry.andromeda.connection.bluetooth

import android.os.Handler
import android.os.Looper
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import java.io.IOException
import java.time.Instant
import kotlin.concurrent.thread

// TODO: Improve the use of threads here
class BluetoothSensor(
    private val device: IBluetoothDevice,
    private val messageHistoryLength: Int
) : AbstractSensor(), IBluetoothSensor {

    override val hasValidReading: Boolean
        get() = messages.isNotEmpty()

    override fun startImpl() {
        connect()
    }

    override fun stopImpl() {
        disconnect()
    }

    override val messages: List<BluetoothMessage>
        get() = _messages

    override val isConnected: Boolean
        get() = device.isConnected()

    private var _messages = listOf<BluetoothMessage>()
    private val handler = Handler(Looper.getMainLooper())

    private fun connect() {
        thread {
            try {
                device.connect()
            } catch (e: IOException) {
                device.disconnect()
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
        device.disconnect()
        notifyListeners()
    }

    override fun write(data: String): Boolean {
        if (!isConnected) return false
        device.write(data)
        return false
    }

    private fun startInputListener() {
        thread {
            while (isConnected) {
                synchronized(device) {
                    try {
                        val recv = device.readLine()
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