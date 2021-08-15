package com.kylecorry.andromeda.sms

import android.Manifest
import android.telephony.SmsManager
import androidx.annotation.RequiresPermission

object SMS {

    @RequiresPermission(Manifest.permission.SEND_SMS)
    fun send(to: String, message: String) {
        val sms = SmsManager.getDefault()
        val parts = sms.divideMessage(message)
        if (parts.size <= 1) {
            sms.sendTextMessage(to, null, message, null, null)
        } else {
            sms.sendMultipartTextMessage(to, null, parts, null, null)
        }
    }

}