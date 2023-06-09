package com.kylecorry.andromeda.core.text

import android.content.Context
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.ImageSpan
import androidx.annotation.ColorInt
import androidx.core.text.inSpans
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.ui.Colors

inline fun SpannableStringBuilder.align(
    alignment: Layout.Alignment,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder =
    inSpans(AlignmentSpan.Standard(alignment), builderAction)

inline fun SpannableStringBuilder.center(builderAction: SpannableStringBuilder.() -> Unit): SpannableStringBuilder =
    align(
        Layout.Alignment.ALIGN_CENTER,
        builderAction
    )

inline fun SpannableStringBuilder.left(builderAction: SpannableStringBuilder.() -> Unit): SpannableStringBuilder =
    align(
        Layout.Alignment.ALIGN_NORMAL,
        builderAction
    )

inline fun SpannableStringBuilder.right(builderAction: SpannableStringBuilder.() -> Unit): SpannableStringBuilder =
    align(
        Layout.Alignment.ALIGN_OPPOSITE,
        builderAction
    )

inline fun SpannableStringBuilder.appendImage(
    context: Context,
    drawableRes: Int,
    width: Int,
    height: Int = width,
    @ColorInt tint: Int? = null,
    flags: Int = ImageSpan.ALIGN_BASELINE
): SpannableStringBuilder {
    val drawable = Resources.drawable(context, drawableRes)
    drawable?.let {
        it.setBounds(0, 0, width, height)
        tint?.let { tint ->
            Colors.setImageColor(it, tint)
        }
        val imageSpan = ImageSpan(it, flags)
        append(" ")
        setSpan(imageSpan, length - 1, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return this
}