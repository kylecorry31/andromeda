package com.kylecorry.andromeda.alerts

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
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
        cancelable: Boolean = true,
        cancelOnOutsideTouch: Boolean = true,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            setCancelable(cancelable)
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
        dialog.setCanceledOnTouchOutside(cancelOnOutsideTouch)
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

        return dialog(
            context,
            title,
            contentView = view,
            okText = null,
            cancelText = null,
            cancelable = false,
            cancelOnOutsideTouch = false
        )
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
        cancelable: Boolean = true,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            setCancelable(cancelable)
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

    fun image(
        context: Context,
        title: String,
        @DrawableRes image: Int
    ): AlertDialog {
        val view = LinearLayout(context)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        view.layoutParams = params
        val imageView = ImageView(context)
        val imageParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        imageParams.gravity = Gravity.CENTER
        imageView.layoutParams = imageParams
        imageView.setImageResource(image)
        view.addView(imageView)

        return dialog(context, title, contentView = view, cancelText = null)
    }

    fun snackbar(
        fragment: Fragment,
        anchorView: View,
        text: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        action: String? = null,
        onAction: () -> Unit = {}
    ): Snackbar {
        return Snackbar.make(fragment.requireView(), text, duration).also {
            if (action != null) {
                it.setAction(action) {
                    onAction()
                }
            }
            it.anchorView = anchorView
            it.show()
        }
    }

    fun snackbar(
        fragment: Fragment,
        @IdRes anchorView: Int,
        text: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        action: String? = null,
        onAction: () -> Unit = {}
    ): Snackbar {
        return Snackbar.make(fragment.requireView(), text, duration).also {
            if (action != null) {
                it.setAction(action) {
                    onAction()
                }
            }
            it.setAnchorView(anchorView)
            it.show()
        }
    }
}