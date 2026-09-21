package com.kylecorry.andromeda.sense.location

import android.location.OnNmeaMessageListener

internal class SimpleNmeaListener(private val onNmeaMessage: (message: String) -> Unit) :
    OnNmeaMessageListener {
    override fun onNmeaMessage(message: String?, timestamp: Long) {
        message ?: return
        onNmeaMessage.invoke(message)
    }
}
