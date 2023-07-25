package com.kylecorry.andromeda.core.ui

import android.os.Build
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.kylecorry.andromeda.core.system.Resources

fun TextView.setCompoundDrawables(
    size: Int? = null,
    @DrawableRes left: Int? = null,
    @DrawableRes top: Int? = null,
    @DrawableRes right: Int? = null,
    @DrawableRes bottom: Int? = null
) {
    val leftDrawable = if (left == null) null else Resources.drawable(context, left)
    val rightDrawable = if (right == null) null else Resources.drawable(context, right)
    val topDrawable = if (top == null) null else Resources.drawable(context, top)
    val bottomDrawable = if (bottom == null) null else Resources.drawable(context, bottom)

    leftDrawable?.setBounds(
        0,
        0,
        size ?: leftDrawable.intrinsicWidth,
        size ?: leftDrawable.intrinsicHeight
    )
    rightDrawable?.setBounds(
        0,
        0,
        size ?: rightDrawable.intrinsicWidth,
        size ?: rightDrawable.intrinsicHeight
    )
    topDrawable?.setBounds(
        0,
        0,
        size ?: topDrawable.intrinsicWidth,
        size ?: topDrawable.intrinsicHeight
    )
    bottomDrawable?.setBounds(
        0,
        0,
        size ?: bottomDrawable.intrinsicWidth,
        size ?: bottomDrawable.intrinsicHeight
    )

    setCompoundDrawables(leftDrawable, topDrawable, rightDrawable, bottomDrawable)
}

fun TextView.setIncognitoMode(isIncognito: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        imeOptions = if (isIncognito) {
            imeOptions.or(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING)
        } else {
            imeOptions.and(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING.inv())
        }
    }
}

/**
 * If the text hasn't changed, this will save some CPU cycles
 */
var TextView.textDistinct: CharSequence?
    set(value){
        if (this.text != value) {
            this.text = value
        }
    }
    get() = text

