package com.kylecorry.andromeda.forms

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.ColorInt

class ButtonField(
    context: Context,
    id: String,
    label: CharSequence,
    @ColorInt textColor: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    fullWidth: Boolean = false,
    onClick: () -> Unit
) :
    FormField<Unit>(id, LinearLayout(context)) {

    private var button: Button = Button(context)
    private var linearLayout = view as LinearLayout

    var label: CharSequence
        get() = button.text
        set(value) {
            button.text = value
        }

    fun setBackgroundColor(@ColorInt color: Int) {
        button.setBackgroundColor(color)
    }

    fun setTextColor(@ColorInt color: Int) {
        button.setTextColor(color)
    }

    var isEnabled: Boolean
        get() = button.isEnabled
        set(value) {
            button.isEnabled = value
        }

    override var value: Unit = Unit

    init {
        Forms.setDefaultLinearLayoutStyle(linearLayout)
        if (!fullWidth) {
            linearLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        Forms.setDefaultFieldPadding(linearLayout)

        linearLayout.addView(button)

        if (textColor != null) {
            setTextColor(textColor)
        }

        if (backgroundColor != null) {
            setBackgroundColor(backgroundColor)
        }

        this.label = label
        button.setOnClickListener {
            onClick.invoke()
        }
    }
}