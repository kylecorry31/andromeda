package com.kylecorry.andromeda.views.reactivity

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.andromeda.views.reactivity.AndroidViewHooks.useAndroidView
import com.kylecorry.andromeda.views.reactivity.AndroidViewHooks.useAndroidViewGroup
import com.kylecorry.luna.annotations.ExperimentalUsage

object AndromedaViews {

    fun ReactiveComponent.Box(
        vararg children: View,
        attributes: ViewAttributes = ViewAttributes()
    ): View {
        return FrameLayout(*children, attributes = attributes)
    }

    fun ReactiveComponent.FrameLayout(
        vararg children: View,
        attributes: ViewAttributes = ViewAttributes()
    ): View {
        return useAndroidViewGroup(*children, attributes = attributes) {
            android.widget.FrameLayout(it)
        }
    }

    open class LinearLayoutAttributes : ViewAttributes() {
        var orientation: Int = LinearLayout.HORIZONTAL
    }

    fun ReactiveComponent.LinearLayout(
        vararg children: View,
        attributes: LinearLayoutAttributes = LinearLayoutAttributes()
    ): View {
        val layout = useAndroidViewGroup(*children, attributes = attributes) {
            LinearLayout(it)
        }

        useEffect(layout, attributes.orientation) {
            layout.orientation = attributes.orientation
        }

        return layout
    }

    fun ReactiveComponent.Column(
        vararg children: View,
        attributes: ViewAttributes = ViewAttributes()
    ): View {
        return LinearLayout(
            *children,
            attributes = attributes {
                orientation = LinearLayout.VERTICAL
                from(attributes)
            }
        )
    }

    fun ReactiveComponent.Row(
        vararg children: View,
        attributes: ViewAttributes = ViewAttributes()
    ): View {
        return LinearLayout(
            *children,
            attributes = attributes {
                orientation = LinearLayout.HORIZONTAL
                from(attributes)
            }
        )
    }

    open class EditTextAttributes : TextAttributes() {
        var hint: String? = null
        var onTextChanged: ((String) -> Unit)? = null
    }

    @ExperimentalUsage("value does not work properly when bound to the same state as the onValueChanged callback")
    fun ReactiveComponent.EditText(
        attributes: EditTextAttributes = EditTextAttributes()
    ): View {
        val view = useAndroidView(attributes) {
            android.widget.EditText(it)
        }

        useEffect(view, attributes.hint) {
            view.hint = attributes.hint
        }

        useEffect(view, attributes.text) {
            view.setText(attributes.text)
            // Set the cursor to the end
            view.setSelection(view.text.length)
        }

        useEffect(view, attributes.onTextChanged) {
            if (attributes.onTextChanged == null) {
                view.addTextChangedListener(null)
            } else {
                view.addTextChangedListener {
                    attributes.onTextChanged?.invoke(it.toString())
                }
            }
        }

        return view
    }

    open class TextAttributes : ViewAttributes() {
        var text: CharSequence? = null
    }

    fun ReactiveComponent.Text(
        attributes: TextAttributes = TextAttributes()
    ): View {
        val view = useAndroidView(attributes) { TextView(it) }

        useEffect(view, attributes.text) {
            view.text = attributes.text
        }
        return view
    }

    open class ButtonAttributes : TextAttributes()

    fun ReactiveComponent.Button(
        attributes: ButtonAttributes = ButtonAttributes()
    ): View {
        val view = useAndroidView(attributes) { android.widget.Button(it) }

        useEffect(view, attributes.text) {
            view.text = attributes.text
        }

        return view
    }

    open class ImageAttributes : ViewAttributes() {
        var imageResource: Int? = null
        var bitmap: android.graphics.Bitmap? = null
        var drawable: android.graphics.drawable.Drawable? = null
        var uri: android.net.Uri? = null
    }

    fun ReactiveComponent.Image(
        attributes: ImageAttributes = ImageAttributes()
    ): View {
        val view = useAndroidView(attributes) { android.widget.ImageView(it) }

        useEffect(
            view,
            attributes.imageResource,
            attributes.bitmap,
            attributes.drawable,
            attributes.uri
        ) {
            when {
                attributes.imageResource != null -> view.setImageResource(attributes.imageResource!!)
                attributes.bitmap != null -> view.setImageBitmap(attributes.bitmap)
                attributes.drawable != null -> view.setImageDrawable(attributes.drawable)
                attributes.uri != null -> view.setImageURI(attributes.uri)
                else -> view.setImageDrawable(null)
            }
        }

        return view
    }

    open class ImageButtonAttributes : ImageAttributes()

    fun ReactiveComponent.ImageButton(
        attributes: ImageButtonAttributes = ImageButtonAttributes()
    ): View {
        val view = useAndroidView(attributes) { android.widget.ImageButton(it) }

        useEffect(
            view,
            attributes.imageResource,
            attributes.bitmap,
            attributes.drawable,
            attributes.uri
        ) {
            when {
                attributes.imageResource != null -> view.setImageResource(attributes.imageResource!!)
                attributes.bitmap != null -> view.setImageBitmap(attributes.bitmap)
                attributes.drawable != null -> view.setImageDrawable(attributes.drawable)
                attributes.uri != null -> view.setImageURI(attributes.uri)
                else -> view.setImageDrawable(null)
            }
        }

        return view
    }

}