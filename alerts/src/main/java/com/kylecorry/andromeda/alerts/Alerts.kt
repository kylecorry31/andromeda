package com.kylecorry.andromeda.alerts

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.ui.Views

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
        scrollable: Boolean = false,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog {
        val builder = dialogBuilder(
            context,
            title,
            content,
            contentView,
            okText,
            cancelText,
            cancelable,
            scrollable,
            onClose
        )

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
        title: CharSequence
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
        scrollable: Boolean = false,
        onClose: ((cancelled: Boolean) -> Unit)? = null
    ): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            setCancelable(cancelable)
            if (content != null && (!scrollable || contentView == null)) {
                setMessage(content)
            }
            if (contentView != null) {
                val view = if (scrollable) {
                    val layout = Views.linear(
                        listOfNotNull(content?.let {
                            Views.text(
                                context,
                                content,
                                id = android.R.id.message
                            )
                        }, contentView),
                        padding = Resources.dp(context, 28f).toInt()
                    )

                    Views.scroll(layout)
                } else {
                    contentView
                }

                setView(view)
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
        title: CharSequence,
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
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_SHORT,
        action: CharSequence? = null,
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
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_SHORT,
        action: CharSequence? = null,
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

    inline fun <T> withLoading(context: Context, title: CharSequence, action: () -> T): T {
        val loadingAlert = loading(context, title)
        try {
            return action()
        } finally {
            loadingAlert.dismiss()
        }
    }

    inline fun <T> withProgress(
        context: Context,
        title: CharSequence,
        action: (setProgress: (Float) -> Unit) -> T
    ): T {
        val container = FrameLayout(context)
        val progressBar = ProgressBar(
            context,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins(32)
            }
            isIndeterminate = false
            max = 100
        }
        container.addView(progressBar)

        val progressDialog = dialog(
            context,
            title,
            contentView = container,
            cancelable = false,
            cancelOnOutsideTouch = false,
            okText = null,
            cancelText = null
        )

        try {
            return action {
                progressBar.post {
                    progressBar.progress = (it * 100).toInt()
                }
            }
        } finally {
            progressDialog.dismiss()
        }
    }
}