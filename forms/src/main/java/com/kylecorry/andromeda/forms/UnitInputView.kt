package com.kylecorry.andromeda.forms

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.toDoubleCompat
import com.kylecorry.andromeda.pickers.Pickers

open class UnitInputView<Units : Enum<*>>(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var _unit: Units? = null
    private var _amount: Number? = null

    override fun isEnabled(): Boolean {
        return amountEdit.isEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        amountEdit.isEnabled = enabled
        unitBtn.isEnabled = enabled
    }

    var units: List<DisplayUnit<Units>> = listOf()
        set(value) {
            field = value
            val unit = this.unit
            if (unit != null && value.none { it.unit == unit }) {
                this.unit = null
            }
        }

    var unit: Units?
        get() = _unit
        set(value) {
            val changed = _unit != value
            _unit = value
            if (changed) {
                setSelectedUnitText(value)
                onChange?.invoke(amount, unit)
            }
        }

    var amount: Number?
        get() = _amount
        set(value) {
            val changed = value != _amount
            _amount = value
            if (changed) {
                setAmountEditText(value)
                onChange?.invoke(amount, unit)
            }
        }

    var hint: CharSequence?
        get() = amountEdit.hint
        set(value) {
            amountEdit.hint = value
        }

    var unitPickerTitle: CharSequence = ""


    var onChange: ((amount: Number?, unit: Units?) -> Unit)? = null

    private var amountEdit = EditText(context)
    private var unitBtn: Button

    private fun setSelectedUnitText(unit: Units?) {
        if (unit != null) {
            val displayUnit = units.firstOrNull { it.unit == unit }
            if (displayUnit == null) {
                _unit = null
                unitBtn.text = ""
            } else {
                unitBtn.text = displayUnit.shortName
            }
        } else {
            unitBtn.text = ""
        }
    }

    private fun setAmountEditText(amount: Number?) {
        amountEdit.setText(amount.toString())
    }

    init {
        amountEdit.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED

        val editParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        amountEdit.layoutParams = editParams

        unitBtn = Button(context)
        val btnParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        btnParams.marginStart = Resources.dp(context, 8f).toInt()
        unitBtn.layoutParams = btnParams
        unitBtn.isAllCaps = false

        addView(amountEdit)
        addView(unitBtn)

        amountEdit.addTextChangedListener {
            _amount = it?.toString()?.toDoubleCompat()
            onChange?.invoke(amount, unit)
        }

        unitBtn.setOnClickListener {
            Pickers.item(
                getContext(),
                unitPickerTitle,
                units.map { it.longName },
                units.indexOfFirst { it.unit == unit }) { idx ->
                if (idx != null) {
                    unit = units[idx].unit
                }
            }
        }
    }

    data class DisplayUnit<Units : Enum<*>>(
        val unit: Units,
        val shortName: String,
        val longName: String
    )

}