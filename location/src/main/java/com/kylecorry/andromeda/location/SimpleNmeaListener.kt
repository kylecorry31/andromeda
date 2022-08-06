@file:Suppress("DEPRECATION")

package com.kylecorry.andromeda.location

import android.location.GpsStatus
import android.location.OnNmeaMessageListener
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
internal class SimpleNmeaListener(private val onNmeaMessage: (message: String) -> Unit) :
    OnNmeaMessageListener {
    override fun onNmeaMessage(message: String?, timestamp: Long) {
        message ?: return
        onNmeaMessage.invoke(message)
    }
}

@Suppress("DEPRECATION")
internal class SimpleLegacyNmeaListener(private val onNmeaMessage: (message: String) -> Unit): GpsStatus.NmeaListener {
    override fun onNmeaReceived(timestamp: Long, nmea: String?) {
        nmea ?: return
        onNmeaMessage.invoke(nmea)
    }

}