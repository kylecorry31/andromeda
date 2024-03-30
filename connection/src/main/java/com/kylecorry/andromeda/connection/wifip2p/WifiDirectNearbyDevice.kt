package com.kylecorry.andromeda.connection.wifip2p

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.connection.NearbyDevice
import com.kylecorry.andromeda.connection.NearbyDeviceMessage
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.system.BroadcastReceiverTopic
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.luna.coroutines.onIO
import com.kylecorry.luna.coroutines.onMain
import com.kylecorry.sol.math.RingBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.annotations.ApiStatus.Experimental
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant

@Experimental
class WifiDirectNearbyDevice(private val device: WifiP2pDevice, private val context: Context) :
    NearbyDevice, AbstractSensor() {

    override val name: String
        get() = device.deviceName
    override val address: String
        get() = device.deviceAddress
    override var isConnected: Boolean = false
        private set
    override var isConnecting: Boolean = false
        private set
    override val messages: List<NearbyDeviceMessage>
        get() = synchronized(messageLock) {
            _messages.toList()
        }

    override var messageHistorySize: Int = 1
        set(value) {
            synchronized(messageLock) {
                field = value
                val newMessages = RingBuffer<NearbyDeviceMessage>(value)
                for (message in _messages.toList()) {
                    newMessages.add(message)
                }
                _messages = newMessages
            }
        }

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val receiver = BroadcastReceiverTopic(
        context, intentFilter, isStickyBroadcast = false
    )

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var _messages = RingBuffer<NearbyDeviceMessage>(messageHistorySize)
    private val messageLock = Any()
    private var hostAddress: String? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var receiverJob: Job? = null

    override suspend fun send(message: ByteArray) = onIO {
        // TODO: Queue messages to send
        // TODO: If this device is the group owner, send to the client
        if (hostAddress == null) {
            return@onIO
        }

        // TODO: Ensure this is encrypted
        Socket().use { socket ->
            socket.bind(null)
            socket.connect(InetSocketAddress(hostAddress, 8888), 500)
            socket.getOutputStream().use { output ->
                output.write(message)
            }
        }
    }

    override val hasValidReading: Boolean
        get() = isConnected

    @SuppressLint("MissingPermission")
    override fun startImpl() {
        if (!hasPermission()) {
            return
        }

        isConnecting = true

        receiver.subscribe(this::onReceiveBroadcast)
        manager = context.getSystemService<WifiP2pManager>() ?: return
        channel = manager?.initialize(context, Looper.getMainLooper(), null) ?: return

        val config = WifiP2pConfig()
        config.deviceAddress = address
        channel?.also { channel ->
            manager?.connect(channel, config, object : WifiP2pManager.ActionListener {

                override fun onSuccess() {
                    println("Connection successful")
                    isConnected = true
                    isConnecting = false

                    notifyListeners()

                    receiverJob = scope.launch {
                        receiveMessages()
                    }
                }

                override fun onFailure(reason: Int) {
                    println("Connection failed")
                    isConnected = false
                    isConnecting = false
                    notifyListeners()
                }
            }
            )
        }

        notifyListeners()
    }

    override fun stopImpl() {
        receiver.unsubscribe(this::onReceiveBroadcast)
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                println("Disconnected")
                isConnected = false
            }

            override fun onFailure(reason: Int) {
                println("Failed to disconnect")
            }
        })

        isConnected = false
        isConnecting = false
        receiverJob?.cancel()
        receiverJob = null
    }

    private suspend fun receiveMessages() = onIO {
        // Listen for messages
        ServerSocket(8888).use { socket ->
            while (isConnected) {
                socket.accept().use { client ->
                    client.getInputStream()?.use {
                        _messages.add(NearbyDeviceMessage(Instant.now(), it.readBytes()))
                        onMain {
                            notifyListeners()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun onReceiveBroadcast(intent: Intent): Boolean {
        val action = intent.action ?: return true

        if (!hasPermission()) {
            return true
        }

        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Determine if Wifi P2P mode is enabled or not, alert the Activity.
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                when (state) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        // Wifi P2P is enabled
                        println("Wifi P2P is enabled")
                    }

                    else -> {
                        println("Wifi P2P is disabled")
                        // Wi-Fi P2P is not enabled
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                manager?.requestConnectionInfo(channel) { info ->
                    Log.d("WIFIP2P", info.groupOwnerAddress?.toString() ?: "No address")
                    hostAddress = info.groupOwnerAddress?.hostAddress
                    if (info.groupFormed && info.isGroupOwner) {
                        Log.d("WIFIP2P", "Connected as group owner")
                    } else if (info.groupFormed) {
                        Log.d("WIFIP2P", "Connected as peer")
                    } else {
                        Log.d("WIFIP2P", "Disconnected")
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                println("This device changed")
                // Respond to this device's wifi state changing
            }
        }


        return true
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Permissions.hasPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) && Permissions.hasPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            Permissions.hasPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}