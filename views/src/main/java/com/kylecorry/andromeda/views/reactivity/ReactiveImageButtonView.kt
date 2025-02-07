package com.kylecorry.andromeda.views.reactivity

import android.content.Context
import android.graphics.drawable.Icon
import androidx.appcompat.widget.AppCompatImageButton

class ReactiveImageButtonView(context: Context) : AppCompatImageButton(context) {

    private var lastIcon: Icon? = null

    fun setIcon(icon: Icon?) {
        if (icon == lastIcon) {
            return
        }

        lastIcon = icon
        setImageIcon(icon)
    }

}