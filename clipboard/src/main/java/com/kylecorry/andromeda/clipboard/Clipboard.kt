package com.kylecorry.andromeda.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import com.kylecorry.andromeda.alerts.Alerts

object Clipboard {

    fun copy(context: Context, text: String, toastMessage: String?) {
        val clipboardManager = context.getSystemService<ClipboardManager>()
        clipboardManager?.setPrimaryClip(ClipData.newPlainText(text, text))

        if (toastMessage != null) {
            Alerts.toast(context, toastMessage, short = true)
        }
    }

}