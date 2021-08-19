package com.kylecorry.andromeda.forms

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.kylecorry.andromeda.core.system.Resources

object Forms {
    class Section(
        context: Context,
        block: Section.() -> Unit = {}
    ) {
        private val fields = mutableMapOf<String, FormField<*>>()

        val view = LinearLayout(context)

        var isVisible: Boolean
            get() = view.isVisible
            set(value) {
                view.isVisible = value
            }

        init {
            setDefaultLinearLayoutStyle(view)
            block(this)
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : FormField<*>> get(id: String): T? {
            return fields[id] as T?
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> getValue(id: String): T? {
            return fields[id]?.value as T?
        }

        fun add(field: FormField<*>, index: Int = -1) {
            fields[field.id] = field
            view.addView(field.view, index)
        }

        fun remove(id: String) {
            val matching = fields[id] ?: return
            fields.remove(id)
            view.removeView(matching.view)
        }

        fun hide(id: String) {
            setVisible(id, false)
        }

        fun show(id: String) {
            setVisible(id, true)
        }

        fun setVisible(id: String, visible: Boolean) {
            val matching = fields[id] ?: return
            matching.view.isVisible = visible
        }

        fun text(
            id: String,
            defaultValue: String? = null,
            label: CharSequence? = null,
            hint: CharSequence? = null,
            onChange: (section: Section, text: String?) -> Unit = { _, _ -> }
        ) {
            add(TextField(view.context, id, defaultValue, label, hint) {
                onChange(this, it)
            })
        }

        fun number(
            id: String,
            defaultValue: Number? = null,
            label: CharSequence? = null,
            allowDecimals: Boolean = true,
            allowNegative: Boolean = false,
            hint: CharSequence? = null,
            onChange: (section: Section, value: Number?) -> Unit = { _, _ -> }
        ) {
            add(
                NumberTextField(
                    view.context,
                    id,
                    defaultValue,
                    label,
                    allowDecimals,
                    allowNegative,
                    hint
                ) {
                    onChange(this, it)
                }
            )
        }

        fun switch(
            id: String,
            defaultValue: Boolean = false,
            label: CharSequence? = null,
            onChange: (section: Section, value: Boolean) -> Unit = { _, _ -> }
        ) {
            add(SwitchField(view.context, id, defaultValue, label) {
                onChange(this, it)
            })
        }

        fun checkbox(
            id: String,
            defaultValue: Boolean = false,
            label: CharSequence? = null,
            onChange: (section: Section, value: Boolean) -> Unit = { _, _ -> }
        ) {
            add(CheckboxField(view.context, id, defaultValue, label) {
                onChange(this, it)
            })
        }

        fun spinner(
            id: String,
            items: List<String>,
            defaultIndex: Int? = null,
            label: CharSequence? = null,
            useDialog: Boolean = false,
            dialogTitle: CharSequence? = label,
            onChange: (section: Section, value: Int?) -> Unit = { _, _ -> }
        ) {
            add(SpinnerField(view.context, id, items, defaultIndex, label, useDialog, dialogTitle) {
                onChange(this, it)
            })
        }

        fun label(
            id: String,
            label: CharSequence,
            @ColorInt color: Int? = null,
            textSizeSp: Float? = null,
            bold: Boolean = false
        ) {
            add(Label(view.context, id, label, color, textSizeSp, bold))
        }

    }

    fun setDefaultLinearLayoutStyle(linearLayout: LinearLayout) {
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    fun setDefaultFieldPadding(view: View) {
        val padding = Resources.dp(view.context, 16f).toInt()
        val paddingTopBtm = Resources.dp(view.context, 10f).toInt()
        view.setPadding(padding, paddingTopBtm, padding, paddingTopBtm)
    }

    fun ViewGroup.add(formSection: Section) {
        addView(formSection.view)
    }

}