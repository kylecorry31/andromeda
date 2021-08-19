package com.kylecorry.andromeda.forms

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible


class Label(
    context: Context,
    id: String,
    label: CharSequence,
    @ColorInt color: Int? = null,
    textSizeSp: Float? = null,
    bold: Boolean = false
) :
    FormField<CharSequence?>(id, TextView(context)) {

    private val textView = view as TextView

    override var value: CharSequence?
        get() = textView.text?.toString()
        set(value) {
            textView.text = value
            textView.isVisible = value != null
        }

    init {
        value = label
        Forms.setDefaultFieldPadding(view)
        if (color != null) {
            textView.setTextColor(color)
        }
        if (textSizeSp != null) {
            textView.textSize = textSizeSp
        }
        if (bold) {
            textView.setTypeface(null, Typeface.BOLD)
        }
    }

}