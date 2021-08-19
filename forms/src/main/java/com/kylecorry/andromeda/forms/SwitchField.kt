package com.kylecorry.andromeda.forms

import android.content.Context
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat

class SwitchField(
    context: Context,
    id: String,
    defaultValue: Boolean = false,
    label: CharSequence? = null,
    var onChange: (value: Boolean) -> Unit = {}
) :
    FormField<Boolean>(id, LinearLayout(context)) {

    private var switchView = SwitchCompat(context)

    var label: CharSequence?
        get() = switchView.text
        set(value) {
            switchView.text = value
        }


    var isEnabled: Boolean
        get() = switchView.isEnabled
        set(value) {
            switchView.isEnabled = value
        }

    override var value: Boolean
        get() = switchView.isChecked
        set(value) {
            switchView.isChecked = value
        }

    init {
        Forms.setDefaultLinearLayoutStyle(view as LinearLayout)
        Forms.setDefaultFieldPadding(view)

        view.addView(switchView)

        this.label = label
        value = defaultValue
        switchView.setOnCheckedChangeListener { _, _ ->
            onChange.invoke(value)
        }
    }
}