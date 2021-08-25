package com.kylecorry.andromeda.core.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

abstract class BaseBroadcastReceiverSensor(
    protected val context: Context,
    private val intentFilter: IntentFilter
) : AbstractSensor() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            context ?: return
            handleIntent(context, intent)
            notifyListeners()
        }
    }

    protected abstract fun handleIntent(context: Context, intent: Intent)

    override fun startImpl() {
        context.registerReceiver(receiver, intentFilter)
    }

    override fun stopImpl() {
        context.unregisterReceiver(receiver)
    }
}