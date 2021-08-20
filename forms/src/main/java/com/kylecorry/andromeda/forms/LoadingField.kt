package com.kylecorry.andromeda.forms

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.kylecorry.andromeda.core.system.Resources

class LoadingField(
    context: Context,
    id: String,
    label: CharSequence? = null
) :
    FormField<Unit>(id, LinearLayout(context)) {

    private var labelView = TextView(context)
    private var progress = CircularProgressIndicator(context)
    private var linearLayout = view as LinearLayout

    var label: CharSequence?
        get() = labelView.text
        set(value) {
            labelView.text = value
            labelView.isVisible = value != null
        }

    override var value: Unit = Unit

    init {
        Forms.setDefaultLinearLayoutStyle(linearLayout)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER_VERTICAL
        Forms.setDefaultFieldPadding(linearLayout)

        val padding = Resources.dp(context, 8f).toInt()
        labelView.setPadding(padding, 0, 0, 0)

        linearLayout.addView(progress)
        linearLayout.addView(labelView)

        progress.isIndeterminate = true

        this.label = label
    }
}