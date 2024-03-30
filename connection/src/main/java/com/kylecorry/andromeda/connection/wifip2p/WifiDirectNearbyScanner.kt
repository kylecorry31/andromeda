package com.kylecorry.andromeda.connection.wifip2p

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Looper
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.connection.NearbyDevice
import com.kylecorry.andromeda.connection.NearbyDeviceScanner
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.system.BroadcastReceiverTopic
import com.kylecorry.andromeda.permissions.Permissions
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
class WifiDirectNearbyScanner(private val context: Context) : NearbyDeviceScanner,
    AbstractSensor() {

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val receiver = BroadcastReceiverTopic(
        context, intentFilter, isStickyBroadcast = false
    )

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    private val _nearbyDevices = mutableListOf<NearbyDevice>()
    private val nearbyDeviceLock = Any()

    override val devices: List<NearbyDevice>
        get() = synchronized(nearbyDeviceLock) {
            _nearbyDevices.toList()
        }

    override var hasValidReading: Boolean = false
        private set

    private val listener = object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            println("Success")
        }

        override fun onFailure(reason: Int) {
            println("Failure")
        }
    }

    @SuppressLint("MissingPermission")
    override fun startImpl() {
        if (!hasPermission()) {
            return
        }

        manager = context.getSystemService<WifiP2pManager>() ?: return
        channel = manager?.initialize(context, Looper.getMainLooper(), null) ?: return
        receiver.subscribe(this::onReceiveBroadcast)
        manager?.discoverPeers(channel, listener)
    }

    override fun stopImpl() {
        receiver.unsubscribe(this::onReceiveBroadcast)
        manager?.stopPeerDiscovery(channel, listener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            channel?.close()
        }
        manager = null
        channel = null
    }

    @SuppressLint("MissingPermission")
    private fun onReceiveBroadcast(intent: Intent): Boolean {
        val action = intent.action ?: return true

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

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager?.requestPeers(channel) { peers: WifiP2pDeviceList? ->
                    hasValidReading = true
                    synchronized(nearbyDeviceLock) {
                        _nearbyDevices.clear()
                        peers?.deviceList?.forEach {
                            _nearbyDevices.add(WifiDirectNearbyDevice(it, context))

                        }
                    }
                    notifyListeners()
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                println("Connection changed")
                // Respond to new connection or disconnections
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