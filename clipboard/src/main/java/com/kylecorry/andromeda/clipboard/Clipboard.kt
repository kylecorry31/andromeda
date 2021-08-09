package com.kylecorry.andromeda.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService

class Clipboard(private val context: Context) : IClipboard {
    private val clipboardManager by lazy { context.getSystemService<ClipboardManager>() }

    override fun copy(text: String, toastMessage: String?) {
        clipboardManager?.setPrimaryClip(ClipData.newPlainText(text, text))

        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

}