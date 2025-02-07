package com.kylecorry.andromeda.views.reactivity

import android.text.TextWatcher
import android.view.View

object AndroidViewHelpers {
    fun createTextWatcher(listener: (text: String) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {
            }
        }
    }

    inline fun <reified T : View> toVDOMNode(view: T): VDOMNode<T, ViewAttributes> {
        return VDOMNode(
            T::class.java,
            ViewAttributes(),
            create = { view },
            update = { _, _ -> }
        )
    }
}