package com.kylecorry.andromeda.alerts

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.dialog(
    title: CharSequence,
    content: CharSequence? = null,
    contentView: View? = null,
    okText: CharSequence? = getString(android.R.string.ok),
    cancelText: CharSequence? = getString(android.R.string.cancel),
    allowLinks: Boolean = false,
    cancelable: Boolean = true,
    cancelOnOutsideTouch: Boolean = true,
    scrollable: Boolean = false,
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
        cancelable,
        cancelOnOutsideTouch,
        scrollable,
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
    cancelable: Boolean = true,
    cancelOnOutsideTouch: Boolean = true,
    scrollable: Boolean = false,
    onClose: ((cancelled: Boolean) -> Unit)? = null
): AlertDialog {
    return Alerts.dialog(
        this,
        title,
        content,
        contentView,
        okText,
        cancelText,
        allowLinks,
        cancelable,
        cancelOnOutsideTouch,
        scrollable,
        onClose
    )
}

fun Fragment.toast(
    text: CharSequence,
    short: Boolean = true
): Toast {
    return Alerts.toast(requireContext(), text, short)
}

fun Fragment.snackbar(
    anchorView: View,
    text: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    action: String? = null,
    onAction: () -> Unit = {}
): Snackbar {
    return Alerts.snackbar(this, anchorView, text, duration, action, onAction)
}

fun Fragment.snackbar(
    @IdRes anchorView: Int,
    text: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    action: String? = null,
    onAction: () -> Unit = {}
): Snackbar {
    return Alerts.snackbar(this, anchorView, text, duration, action, onAction)
}