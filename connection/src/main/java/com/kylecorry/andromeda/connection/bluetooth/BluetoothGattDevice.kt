package com.kylecorry.andromeda.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.tryOrNothing
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BluetoothGattDevice(private val context: Context, val address: String) {

    private val adapter by lazy { context.getSystemService<BluetoothManager>()?.adapter }
    private val device by lazy { adapter?.getRemoteDevice(address) }
    private var gatt: BluetoothGatt? = null

    private val readReceivers = mutableMapOf<UUID, (ByteArray?) -> Unit>()
    private val writeCharacteristicsReceiver = mutableMapOf<UUID, (Boolean) -> Unit>()
    private val changeCharacteristicsReceiver = mutableMapOf<UUID, (ByteArray) -> Unit>()
    private val writeDescriptorReceivers = mutableMapOf<UUID, (Boolean) -> Unit>()
    private var connectionReceiver: (Boolean) -> Unit = {}
    private var onServicesDiscovered: (Boolean) -> Unit = {}

    private var characteristics = emptyList<BluetoothGattCharacteristic>()

    private var isConnected = false

    // TODO: Make the callbacks thread safe
    private val callback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                readReceivers[characteristic.uuid]?.invoke(null)
                return
            }

            readReceivers[characteristic.uuid]?.invoke(value)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            writeCharacteristicsReceiver[characteristic?.uuid]?.invoke(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            writeDescriptorReceivers[descriptor?.uuid]?.invoke(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            changeCharacteristicsReceiver[characteristic.uuid]?.invoke(value)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            connectionReceiver(newState == BluetoothGatt.STATE_CONNECTED)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            characteristics = gatt?.services?.flatMap { it.characteristics } ?: emptyList()
            onServicesDiscovered(status == BluetoothGatt.GATT_SUCCESS)
        }
    }

    val name: String
        @SuppressLint("MissingPermission")
        get() = device?.name ?: ""

    // TODO: Make this thread safe
    // TODO: Let the consumer know it requires a permission
    @SuppressLint("MissingPermission")
    suspend fun connect(): Boolean = suspendCoroutine {
        if (isConnected() || adapter?.isEnabled != true || device == null) {
            it.resume(false)
            return@suspendCoroutine
        }
        adapter?.cancelDiscovery()
        connectionReceiver = { status: Boolean ->
            isConnected = status
            // Replace the connection receiver
            connectionReceiver = {
                isConnected = it
            }
            if (!status) {
                it.resume(false)
            } else {
                onServicesDiscovered = { discoveryStatus ->
                    it.resume(discoveryStatus)
                }
                gatt?.discoverServices()
            }
        }
        gatt = device?.connectGatt(context, false, callback)
        try {
            gatt?.connect()
        } catch (e: Exception) {
            disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        tryOrNothing {
            isConnected = false
            gatt?.disconnect()
            gatt?.close()
            gatt = null
        }
    }

    fun isConnected(): Boolean {
        return isConnected
    }

    // Reading
    @SuppressLint("MissingPermission")
    suspend fun read(characteristicUUID: UUID): ByteArray? = suspendCoroutine {
        if (!isConnected()) {
            it.resume(null)
            return@suspendCoroutine
        }

        val characteristic = characteristics.firstOrNull { it.uuid == characteristicUUID }

        if (characteristic == null) {
            it.resume(null)
            return@suspendCoroutine
        }

        readReceivers[characteristicUUID] = { bytes ->
            it.resume(bytes)
            readReceivers.remove(characteristicUUID)
        }
        gatt?.readCharacteristic(characteristic)
    }

    // Writing
    suspend fun write(characteristicUUID: UUID, str: String): Boolean {
        return write(characteristicUUID, str.toByteArray())
    }

    // TODO: Use channel / flow
    @SuppressLint("MissingPermission")
    suspend fun setNotificationListener(
        characteristicUUID: UUID,
        shouldWriteDescriptor: Boolean = true,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
        listener: ((ByteArray) -> Unit)?
    ): Boolean = suspendCoroutine {
        if (!isConnected()) {
            it.resume(false)
            return@suspendCoroutine
        }
        val characteristic = characteristics.firstOrNull { it.uuid == characteristicUUID }

        if (characteristic == null) {
            it.resume(false)
            return@suspendCoroutine
        }

        // Set the write type
        characteristic.writeType = writeType

        // Enable notifications
        val wasUpdated = gatt?.setCharacteristicNotification(characteristic, listener != null)

        if (!wasUpdated!!) {
            it.resume(false)
            return@suspendCoroutine
        }

        // Add the listener
        changeCharacteristicsReceiver[characteristicUUID] = listener ?: {}

        if (shouldWriteDescriptor) {
            // Write the descriptor to enable notifications
            writeDescriptorReceivers[characteristicUUID] = { descStatus ->
                writeDescriptorReceivers.remove(characteristicUUID)
                it.resume(descStatus)
            }
            // Set the descriptor
            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            )

            val value = if (listener != null) {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val status = gatt?.writeDescriptor(descriptor, value)
                if (status != BluetoothGatt.GATT_SUCCESS){
                    it.resume(false)
                }
            } else {
                @Suppress("DEPRECATION")
                descriptor?.value = value
                @Suppress("DEPRECATION")
                val status = gatt?.writeDescriptor(descriptor)
                if (status != true){
                    it.resume(false)
                }
            }
        } else {
            it.resume(true)
        }
    }

    // TODO: Make write type an enum
    @SuppressLint("MissingPermission")
    suspend fun write(
        characteristicUUID: UUID,
        bytes: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ): Boolean =
        suspendCoroutine {
            if (!isConnected()) {
                it.resume(false)
                return@suspendCoroutine
            }
            val characteristic = characteristics.firstOrNull { it.uuid == characteristicUUID }

            if (characteristic == null) {
                it.resume(false)
                return@suspendCoroutine
            }

            writeCharacteristicsReceiver[characteristicUUID] = { _ ->
                it.resume(true)
                writeCharacteristicsReceiver.remove(characteristicUUID)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt?.writeCharacteristic(
                    characteristic,
                    bytes,
                    writeType
                )
            } else {
                @Suppress("DEPRECATION")
                characteristic.value = bytes
                characteristic.writeType = writeType
                @Suppress("DEPRECATION")
                gatt?.writeCharacteristic(characteristic)
            }
        }
}