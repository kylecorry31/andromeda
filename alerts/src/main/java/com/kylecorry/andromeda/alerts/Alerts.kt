package com.kylecorry.andromeda.alerts

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.kylecorry.andromeda.core.system.Resources

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
        dialog.show()
        if (allowLinks) {
            dialog.findViewById<TextView>(android.R.id.message)?.movementMethod =
                LinkMovementMethod.getInstance()
        }
        return dialog
    }

    fun loading(
        context: Context,
        title: String
    ): AlertDialog {
        val view = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        view.layoutParams = params
        val loading = CircularProgressIndicator(context)
        loading.isIndeterminate = true

        val loadingParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        loadingParams.bottomMargin = Resources.dp(context, 16f).toInt()
        loadingParams.topMargin = Resources.dp(context, 16f).toInt()
        loading.layoutParams = loadingParams
        view.addView(loading)

        val dialog = dialog(context, title, contentView = view, okText = null, cancelText = null)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
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