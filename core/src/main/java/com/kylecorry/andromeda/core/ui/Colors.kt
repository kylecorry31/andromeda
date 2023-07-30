package com.kylecorry.andromeda.core.ui

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import kotlin.math.absoluteValue

object Colors {

    @ColorInt
    fun mostContrastingColor(
        @ColorInt foreground1: Int,
        @ColorInt foreground2: Int,
        @ColorInt background: Int
    ): Int {
        // From https://newbedev.com/how-to-programmatically-calculate-the-contrast-ratio-between-two-colors
        val f1 = (299 * Color.red(foreground1) + 587 * Color.green(foreground1) + 114 * Color.blue(
            foreground1
        )) / 1000f

        val f2 = (299 * Color.red(foreground2) + 587 * Color.green(foreground2) + 114 * Color.blue(
            foreground2
        )) / 1000f

        val b = (299 * Color.red(background) + 587 * Color.green(background) + 114 * Color.blue(
            background
        )) / 1000f

        val r1 = f1 - b
        val r2 = f2 - b

        return if (r1.absoluteValue > r2.absoluteValue) foreground1 else foreground2
    }

    fun setImageColor(view: ImageView, @ColorInt color: Int?) {
        if (color == null) {
            view.clearColorFilter()
            return
        }
        view.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    fun setImageColor(drawable: Drawable, @ColorInt color: Int?) {
        if (color == null) {
            drawable.clearColorFilter()
            return
        }
        drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    fun setImageColor(textView: TextView, @ColorInt color: Int?) {
        textView.compoundDrawables.forEach {
            it?.let { setImageColor(it, color) }
        }
    }

    @ColorInt
    fun Int.withAlpha(alpha: Int): Int {
        return Color.argb(
            alpha,
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    }

    fun toHex(@ColorInt color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

}