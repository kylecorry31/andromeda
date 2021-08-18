package com.kylecorry.andromeda.alerts

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.dialog(
    title: CharSequence,
    content: CharSequence? = null,
    contentView: View? = null,
    okText: CharSequence? = getString(android.R.string.ok),
    cancelText: CharSequence? = getString(android.R.string.cancel),
    allowLinks: Boolean = false,
    onClose: ((cancelled: Boolean) -> Unit)? = null
): AlertDialog {
    return Alerts.dialog(
        requireContext(),
        title,
        content,
        contentView,
        okText,
        cancelText,
        allowLinks,
        onClose
    )
}

fun Activity.dialog(
    title: CharSequence,
    content: CharSequence? = null,
    contentView: View? = null,
    okText: CharSequence? = getString(android.R.string.ok),
    cancelText: CharSequence? = getString(android.R.string.cancel),
    allowLinks: Boolean = false,
    onClose: ((cancelled: Boolean) -> Unit)? = null
): AlertDialog {
    return Alerts.dialog(this, title, content, contentView, okText, cancelText, allowLinks, onClose)
}

fun Fragment.toast(
    text: CharSequence,
    short: Boolean = true
): Toast {
    return Alerts.toast(requireContext(), text, short)
}