package com.kylecorry.andromeda.sms

import android.Manifest
import android.telephony.SmsManager
import androidx.annotation.RequiresPermission

class SmsService {

    @RequiresPermission(Manifest.permission.SEND_SMS)
    fun send(to: String, message: String) {
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(to, null, message, null, null)
    }

}