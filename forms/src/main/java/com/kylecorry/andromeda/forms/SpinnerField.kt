package com.kylecorry.andromeda.forms

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isVisible
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.pickers.Pickers

@SuppressLint("ClickableViewAccessibility")
class SpinnerField(
    context: Context,
    id: String,
    private val items: List<String>,
    defaultIndex: Int? = null,
    label: CharSequence? = null,
    useDialog: Boolean = false,
    dialogTitle: CharSequence? = label,
    var onChanged: (index: Int?) -> Unit = {}
) :
    FormField<Int?>(id, LinearLayout(context)) {

    private var labelView: TextView = TextView(context)
    private var spinnerView =
        AppCompatSpinner(context, if (useDialog) Spinner.MODE_DIALOG else Spinner.MODE_DROPDOWN)
    private var linearLayout = view as LinearLayout

    var label: CharSequence?
        get() = labelView.text
        set(value) {
            labelView.text = value
            labelView.isVisible = value != null
        }

    var isEnabled: Boolean
        get() = spinnerView.isEnabled
        set(value) {
            spinnerView.isEnabled = value
        }

    override var value: Int?
        get() {
            val pos = spinnerView.selectedItemPosition
            if (pos >= 0 && pos <= items.size) {
                return pos
            }
            return null
        }
        set(value) {
            if (value != null) {
                spinnerView.setSelection(value)
            } else {
                spinnerView.setSelection(-1)
            }
        }

    init {
        Forms.setDefaultLinearLayoutStyle(linearLayout)
        Forms.setDefaultFieldPadding(linearLayout)
        val labelSpacing = Resources.dp(context, 8f).toInt()
        labelView.setPadding(0, 0, 0, labelSpacing)

        linearLayout.addView(labelView)
        linearLayout.addView(spinnerView)

        this.label = label

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            items
        )
        if (useDialog) {
            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice)
        } else {
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        if (useDialog) {
            spinnerView.setOnTouchListener { _, event ->
                if (event.action != MotionEvent.ACTION_DOWN) {
                    return@setOnTouchListener true
                }
                Pickers.item(context, dialogTitle ?: "", items, value ?: -1) {
                    if (it != null) {
                        value = it
                    }
                }
                true
            }
        }

        spinnerView.prompt = dialogTitle

        spinnerView.adapter = adapter

        value = defaultIndex

        spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onChanged.invoke(value)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                onChanged.invoke(value)
            }

        }
    }
}