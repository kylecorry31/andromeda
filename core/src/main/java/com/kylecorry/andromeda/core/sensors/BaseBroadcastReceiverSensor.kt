package com.kylecorry.andromeda.core.sensors

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.kylecorry.andromeda.core.system.BroadcastReceiverTopic

abstract class BaseBroadcastReceiverSensor(
    protected val context: Context,
    intentFilter: IntentFilter,
    listenToBroadcastsFromOtherApps: Boolean = false,
    isStickyBroadcast: Boolean = false
) : AbstractSensor() {

    private val receiver = BroadcastReceiverTopic(
        context,
        intentFilter,
        listenToBroadcastsFromOtherApps,
        isStickyBroadcast
    )

    protected abstract fun handleIntent(context: Context, intent: Intent)

    override fun startImpl() {
        receiver.subscribe(this::onReceive)
    }

    override fun stopImpl() {
        receiver.unsubscribe(this::onReceive)
    }

    protected fun onReceive(intent: Intent): Boolean {
        handleIntent(context, intent)
        notifyListeners()
        return true
    }
}