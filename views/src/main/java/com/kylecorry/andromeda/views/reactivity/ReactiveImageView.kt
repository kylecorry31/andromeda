package com.kylecorry.andromeda.views.reactivity

import android.content.Context
import android.graphics.drawable.Icon
import android.text.TextWatcher
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView

class ReactiveImageView(context: Context) : AppCompatImageView(context) {

    private var lastIcon: Icon? = null

    fun setIcon(icon: Icon?) {
        if (icon == lastIcon) {
            return
        }

        lastIcon = icon
        setImageIcon(icon)
    }

}