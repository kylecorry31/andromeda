package com.kylecorry.andromeda.core.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.Button
import android.widget.ImageButton
import androidx.annotation.ColorInt
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.ui.Colors.setImageColor

fun ImageButton.flatten() {
    backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    elevation = 0f
}

fun ImageButton.setState(
    isOn: Boolean,
    @ColorInt primaryColor: Int,
    @ColorInt secondaryColor: Int
) {
    backgroundTintList = if (isOn) {
        drawable?.let { setImageColor(it, secondaryColor) }
        ColorStateList.valueOf(primaryColor)
    } else {
        drawable?.let { setImageColor(it, Resources.androidTextColorSecondary(context)) }
        ColorStateList.valueOf(Resources.androidBackgroundColorSecondary(context))
    }
}

fun Button.setState(
    isOn: Boolean,
    @ColorInt primaryColor: Int,
    @ColorInt secondaryColor: Int
) {
    backgroundTintList = if (isOn) {
        setTextColor(secondaryColor)
        ColorStateList.valueOf(primaryColor)
    } else {
        setTextColor(Resources.androidTextColorSecondary(context))
        ColorStateList.valueOf(Resources.androidBackgroundColorSecondary(context))
    }
}

