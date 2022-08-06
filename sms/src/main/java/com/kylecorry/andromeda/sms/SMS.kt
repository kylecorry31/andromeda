package com.kylecorry.andromeda.sms

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.core.system.Android

object SMS {

    @RequiresPermission(Manifest.permission.SEND_SMS)
    fun send(context: Context, to: String, message: String) {
        val sms = getManager(context) ?: return
        val parts = sms.divideMessage(message)
        if (parts.size <= 1) {
            sms.sendTextMessage(to, null, message, null, null)
        } else {
            sms.sendMultipartTextMessage(to, null, parts, null, null)
        }
    }

    private fun getManager(context: Context): SmsManager? {
        return if (Android.sdk < Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        } else {
            context.getSystemService()
        }
    }

}