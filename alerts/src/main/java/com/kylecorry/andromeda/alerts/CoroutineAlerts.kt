package com.kylecorry.andromeda.alerts

import android.content.Context
import android.view.View
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object CoroutineAlerts {

    suspend fun dialog(
        context: Context,
        title: CharSequence,
        content: CharSequence? = null,
        contentView: View? = null,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel),
        cancelable: Boolean = true,
        cancelOnOutsideTouch: Boolean = true,
        allowLinks: Boolean = false
    ) = suspendCoroutine<Boolean> { cont ->
        Alerts.dialog(
            context,
            title,
            content,
            contentView,
            okText,
            cancelText,
            allowLinks,
            cancelable,
            cancelOnOutsideTouch
        ) {
            cont.resume(it)
        }
    }
}