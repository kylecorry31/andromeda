package com.kylecorry.andromeda.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage

abstract class SmsReceiver : BroadcastReceiver() {
    @Suppress("DEPRECATION")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras ?: return
            try {
                val format = bundle.getString("format")
                val messages = mutableListOf<SmsMessage>()
                val pdus = bundle.get("pdus") as Array<*>
                for (i in pdus.indices) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        messages.add(SmsMessage.createFromPdu(pdus[i] as ByteArray, format))
                    } else {
                        messages.add(SmsMessage.createFromPdu(pdus[i] as ByteArray))
                    }
                }
                messages.forEach {
                    val message = IncomingSmsMessage(it.originatingAddress ?: "", it.messageBody)
                    onMessageReceived(message)
                }
            } catch (e: Exception) {
            }
        }
    }

    abstract fun onMessageReceived(message: IncomingSmsMessage)

}