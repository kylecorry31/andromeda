package com.kylecorry.andromeda.alerts

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

fun Fragment.dialog(
    title: CharSequence,
    content: CharSequence? = null,
    contentView: View? = null,
    okText: CharSequence? = getString(android.R.string.ok),
    cancelText: CharSequence? = getString(android.R.string.cancel),
    onClose: ((cancelled: Boolean) -> Unit)? = null
): AlertDialog {
    return Alerts.dialog(requireContext(), title, content, contentView, okText, cancelText, onClose)
}

fun Fragment.toast(
    text: CharSequence,
    short: Boolean = true
): Toast {
    return Alerts.toast(requireContext(), text, short)
}