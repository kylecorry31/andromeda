package com.kylecorry.andromeda.views.reactivity

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.andromeda.views.reactivity.AndroidViewHooks.useAndroidView
import com.kylecorry.andromeda.views.reactivity.AndroidViewHooks.useLayout
import com.kylecorry.andromeda.views.reactivity.AndroidViewHooks.useStyle
import com.kylecorry.luna.annotations.ExperimentalUsage

object AndromedaViews {

    fun ReactiveComponent.LinearLayout(
        vararg children: View,
        orientation: Int = LinearLayout.VERTICAL,
        style: AndromedaStyle = AndromedaStyle()
    ): View {
        val layout = useAndroidView {
            LinearLayout(it).also {
                it.orientation = orientation
                it.layoutParams = ViewGroup.LayoutParams(style.width, style.height)
            }
        }

        useStyle(layout, style)
        useLayout(layout, children)

        return layout
    }

    fun ReactiveComponent.Column(
        vararg children: View,
        style: AndromedaStyle = AndromedaStyle()
    ): View {
        return LinearLayout(*children, orientation = LinearLayout.VERTICAL, style = style)
    }

    fun ReactiveComponent.Row(
        vararg children: View,
        style: AndromedaStyle = AndromedaStyle()
    ): View {
        return LinearLayout(
            *children,
            orientation = LinearLayout.HORIZONTAL,
            style = style
        )
    }

    @ExperimentalUsage("value does not work properly when bound to the same state as the onValueChanged callback")
    fun ReactiveComponent.EditText(
        hint: String? = null,
        value: String? = null,
        style: AndromedaStyle = AndromedaStyle(),
        onValueChanged: (String) -> Unit = {}
    ): View {
        val view = useAndroidView {
            EditText(it)
        }

        useEffect(view, hint) {
            view.hint = hint
        }

        useEffect(view, value) {
            view.setText(value)
            // Set the cursor to the end
            view.setSelection(view.text.length)
        }

        useEffect(view, onValueChanged) {
            view.addTextChangedListener {
                onValueChanged(it.toString())
            }
        }

        useStyle(view, style)

        return view
    }

    fun ReactiveComponent.Text(text: CharSequence? = null, style: AndromedaStyle = AndromedaStyle()): View {
        val view = useAndroidView { TextView(it) }

        useEffect(view, text) {
            view.text = text
        }

        useStyle(view, style)
        return view
    }
}