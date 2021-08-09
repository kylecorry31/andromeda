package com.kylecorry.andromeda.sms

import android.Manifest
import android.content.Context
import android.telephony.SmsManager
import androidx.annotation.RequiresPermission

class SmsService(private val context: Context) {

    @RequiresPermission(Manifest.permission.SEND_SMS)
    fun send(to: String, message: String) {
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(to, null, message, null, null)
    }

}