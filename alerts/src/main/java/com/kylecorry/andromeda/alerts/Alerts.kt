package com.kylecorry.andromeda.alerts

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object Alerts {
    fun dialog(
        context: Context,
        title: CharSequence,
        content: CharSequence? = null,
        contentView: View? = null,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel),
        allowLinks: Boolean = false,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            if (content != null) {
                setMessage(content)
            }
            if (contentView != null) {
                setView(contentView)
            }
            if (okText != null) {
                setPositiveButton(
                    okText
                ) { dialog, _ ->
                    onClose?.invoke(false)
                    dialog.dismiss()
                }
            }
            if (cancelText != null) {
                setNegativeButton(
                    cancelText
                ) { dialog, _ ->
                    onClose?.invoke(true)
                    dialog.dismiss()
                }
            }
            setOnCancelListener {
                onClose?.invoke(true)
            }
        }

        val dialog = builder.create()
        if (allowLinks) {
            dialog.findViewById<TextView>(android.R.id.message)?.movementMethod =
                LinkMovementMethod.getInstance()
        }
        dialog.show()
        return dialog
    }

    fun toast(
        context: Context,
        text: CharSequence,
        short: Boolean = true
    ): Toast {
        val toast =
            Toast.makeText(context, text, if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
        toast.show()
        return toast
    }

    fun dialogBuilder(
        context: Context,
        title: CharSequence,
        content: CharSequence? = null,
        contentView: View? = null,
        okText: CharSequence? = context.getString(android.R.string.ok),
        cancelText: CharSequence? = context.getString(android.R.string.cancel),
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            if (content != null) {
                setMessage(content)
            }
            if (contentView != null) {
                setView(contentView)
            }
            if (okText != null) {
                setPositiveButton(
                    okText
                ) { dialog, _ ->
                    onClose?.invoke(false)
                    dialog.dismiss()
                }
            }
            if (cancelText != null) {
                setNegativeButton(
                    cancelText
                ) { dialog, _ ->
                    onClose?.invoke(true)
                    dialog.dismiss()
                }
            }
            setOnCancelListener {
                onClose?.invoke(true)
            }
        }

        return builder
    }
}