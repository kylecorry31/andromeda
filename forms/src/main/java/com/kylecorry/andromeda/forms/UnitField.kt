package com.kylecorry.andromeda.forms

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.kylecorry.andromeda.core.system.Resources

class UnitField<Units : Enum<*>>(
    context: Context,
    id: String,
    units: List<UnitInputView.DisplayUnit<Units>>,
    defaultValue: Pair<Number?, Units?>? = null,
    label: CharSequence? = null,
    hint: CharSequence? = null,
    dialogTitle: CharSequence? = null,
    var onChanged: (value: Pair<Number?, Units?>?) -> Unit = {}
) :
    FormField<Pair<Number?, Units?>?>(id, LinearLayout(context)) {

    private var labelView: TextView = TextView(context)
    private var unitInputView = UnitInputView<Units>(context)
    private var linearLayout = view as LinearLayout

    var label: CharSequence?
        get() = labelView.text
        set(value) {
            labelView.text = value
            labelView.isVisible = value != null
        }


    var isEnabled: Boolean
        get() = unitInputView.isEnabled
        set(value) {
            unitInputView.isEnabled = value
        }

    override var value: Pair<Number?, Units?>?
        get() = unitInputView.amount to unitInputView.unit
        set(value) {
            unitInputView.amount = value?.first
            unitInputView.unit = value?.second
        }

    init {
        Forms.setDefaultLinearLayoutStyle(linearLayout)
        Forms.setDefaultFieldPadding(linearLayout)
        val labelSpacing = Resources.dp(context, 8f).toInt()
        labelView.setPadding(0, 0, 0, labelSpacing)

        linearLayout.addView(labelView)
        linearLayout.addView(unitInputView)

        this.label = label
        unitInputView.units = units
        unitInputView.hint = hint
        unitInputView.unitPickerTitle = dialogTitle ?: ""
        value = defaultValue
        unitInputView.onChange = { _, _ ->
            onChanged.invoke(value)
        }
    }
}