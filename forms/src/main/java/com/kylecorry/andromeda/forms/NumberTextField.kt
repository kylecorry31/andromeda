package com.kylecorry.andromeda.forms

import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.toDoubleCompat

class NumberTextField(
    context: Context,
    id: String,
    defaultValue: Number? = null,
    label: CharSequence? = null,
    allowDecimals: Boolean = true,
    allowNegative: Boolean = false,
    hint: CharSequence? = null,
    var onChanged: (value: Number?) -> Unit = {}
) :
    FormField<Number?>(id, LinearLayout(context)) {

    private var labelView: TextView = TextView(context)
    private var editView = EditText(context)
    private var linearLayout = view as LinearLayout

    var error: CharSequence?
        get() = editView.error
        set(value) {
            editView.error = value
        }

    var label: CharSequence?
        get() = labelView.text
        set(value) {
            labelView.text = value
            labelView.isVisible = value != null
        }

    var hint: CharSequence?
        get() = editView.hint
        set(value) {
            editView.hint = value
        }

    var isEnabled: Boolean
        get() = editView.isEnabled
        set(value) {
            editView.isEnabled = value
        }

    override var value: Number?
        get() = editView.text.toString().toDoubleCompat()
        set(value) {
            editView.setText(value?.toString())
        }

    init {
        Forms.setDefaultLinearLayoutStyle(linearLayout)
        Forms.setDefaultFieldPadding(linearLayout)
        val labelSpacing = Resources.dp(context, 8f).toInt()
        labelView.setPadding(0, 0, 0, labelSpacing)
        editView.width = LinearLayout.LayoutParams.MATCH_PARENT
        editView.inputType = InputType.TYPE_CLASS_NUMBER or
                (if (allowDecimals) InputType.TYPE_NUMBER_FLAG_DECIMAL else 0) or
                (if (allowNegative) InputType.TYPE_NUMBER_FLAG_SIGNED else 0)

        linearLayout.addView(labelView)
        linearLayout.addView(editView)

        this.label = label
        this.hint = hint
        value = defaultValue
        editView.addTextChangedListener {
            onChanged.invoke(value)
        }
    }
}