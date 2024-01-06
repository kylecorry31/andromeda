package com.kylecorry.andromeda.views.toolbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.ui.Colors.setImageColor
import com.kylecorry.andromeda.views.R

class Toolbar(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val leftButton: ImageButton
    val rightButton: ImageButton
    val title: TextView
    val subtitle: TextView

    init {
        inflate(context, R.layout.andromeda_view_toolbar, this)
        leftButton = findViewById(R.id.andromeda_toolbar_left_button)
        rightButton = findViewById(R.id.andromeda_toolbar_right_button)
        title = findViewById(R.id.andromeda_toolbar_title)
        subtitle = findViewById(R.id.andromeda_toolbar_subtitle)

        // Update attributes
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.AndromedaToolbar, 0, 0)
        title.text = a.getString(R.styleable.AndromedaToolbar_title) ?: ""
        subtitle.text = a.getString(R.styleable.AndromedaToolbar_subtitle) ?: ""

        subtitle.isVisible = a.getBoolean(R.styleable.AndromedaToolbar_showSubtitle, true)

        val leftIcon = a.getResourceId(R.styleable.AndromedaToolbar_leftButtonIcon, -1)
        val rightIcon = a.getResourceId(R.styleable.AndromedaToolbar_rightButtonIcon, -1)
        val iconColor = a.getColor(
            R.styleable.AndromedaToolbar_iconForegroundColor,
            Resources.androidTextColorSecondary(context)
        )
        val iconBackgroundColor = a.getColor(
            R.styleable.AndromedaToolbar_iconBackgroundColor,
            Resources.androidBackgroundColorSecondary(context)
        )

        if (leftIcon != -1) {
            leftButton.isVisible = true
            leftButton.setImageResource(leftIcon)
        }

        if (rightIcon != -1) {
            rightButton.isVisible = true
            rightButton.setImageResource(rightIcon)
        }

        updateButtonColor(rightButton, iconColor, iconBackgroundColor)
        updateButtonColor(leftButton, iconColor, iconBackgroundColor)

        val flattenQuickActions = a.getBoolean(R.styleable.AndromedaToolbar_flattenButtons, false)
        if (flattenQuickActions) {
            rightButton.flatten()
            leftButton.flatten()
        }
    }

    private fun ImageButton.flatten() {
        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        elevation = 0f
    }

    private fun updateButtonColor(
        button: ImageButton,
        @ColorInt foreground: Int,
        @ColorInt background: Int
    ) {
        button.drawable?.let {
            setImageColor(it, foreground)
        }
        button.backgroundTintList = ColorStateList.valueOf(background)
    }

}