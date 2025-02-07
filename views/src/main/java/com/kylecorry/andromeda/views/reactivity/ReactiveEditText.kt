package com.kylecorry.andromeda.views.reactivity

import android.content.Context
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText

class ReactiveEditText(context: Context) : AppCompatEditText(context) {

    private var lastListener: TextWatcher? = null

    fun setTextChangedListener(listener: TextWatcher?) {
        if (lastListener == listener) {
            return
        }

        lastListener?.let { removeTextChangedListener(listener) }
        if (listener != null) {
            addTextChangedListener(listener)
        }
    }

}