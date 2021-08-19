package com.kylecorry.andromeda.forms

import android.content.Context
import android.widget.CheckBox
import android.widget.LinearLayout

class CheckboxField(
    context: Context,
    id: String,
    defaultValue: Boolean = false,
    label: CharSequence? = null,
    var onChange: (value: Boolean) -> Unit = {}
) :
    FormField<Boolean>(id, LinearLayout(context)) {

    private var checkboxView = CheckBox(context)
    private var layout = view as LinearLayout

    var label: CharSequence?
        get() = checkboxView.text
        set(value) {
            checkboxView.text = value
        }


    var isEnabled: Boolean
        get() = checkboxView.isEnabled
        set(value) {
            checkboxView.isEnabled = value
        }

    override var value: Boolean
        get() = checkboxView.isChecked
        set(value) {
            checkboxView.isChecked = value
        }

    init {
        Forms.setDefaultLinearLayoutStyle(layout)
        Forms.setDefaultFieldPadding(layout)

        layout.addView(checkboxView)

        this.label = label
        value = defaultValue
        checkboxView.setOnCheckedChangeListener { _, _ ->
            onChange.invoke(value)
        }
    }
}