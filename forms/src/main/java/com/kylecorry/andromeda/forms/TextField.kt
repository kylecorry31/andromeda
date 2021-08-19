package com.kylecorry.andromeda.forms

import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.kylecorry.andromeda.core.system.Resources

class TextField(
    context: Context,
    id: String,
    defaultValue: String? = null,
    label: CharSequence? = null,
    hint: CharSequence? = null,
    var onTextChanged: (text: String?) -> Unit = {}
) :
    FormField<String?>(id, LinearLayout(context)) {

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

    override var value: String?
        get() = editView.text.toString()
        set(value) {
            editView.setText(value)
        }

    init {
        Forms.setDefaultLinearLayoutStyle(linearLayout)
        Forms.setDefaultFieldPadding(linearLayout)
        val labelSpacing = Resources.dp(context, 8f).toInt()
        labelView.setPadding(0, 0, 0, labelSpacing)
        editView.width = LinearLayout.LayoutParams.MATCH_PARENT

        linearLayout.addView(labelView)
        linearLayout.addView(editView)

        this.label = label
        this.hint = hint
        value = defaultValue
        editView.addTextChangedListener {
            onTextChanged.invoke(value)
        }
    }
}