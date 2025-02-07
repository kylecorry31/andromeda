package com.kylecorry.andromeda.views.reactivity

import android.graphics.drawable.Icon
import android.text.TextWatcher
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.kylecorry.andromeda.core.ui.ReactiveComponent
import com.kylecorry.andromeda.views.list.AndromedaListView
import com.kylecorry.andromeda.views.list.ListItem

object AndromedaViews {

    fun Box(
        vararg children: VDOMNode<*, *>
    ): VDOMNode<FrameLayout, ViewAttributes> {
        return Box({}, *children)
    }

    fun Box(
        config: ViewAttributes.() -> Unit,
        vararg children: VDOMNode<*, *>
    ): VDOMNode<FrameLayout, ViewAttributes> {
        return FrameLayout(config, *children)
    }

    fun FrameLayout(
        vararg children: VDOMNode<*, *>
    ): VDOMNode<FrameLayout, ViewAttributes> {
        return FrameLayout({}, *children)
    }

    fun FrameLayout(
        config: ViewAttributes.() -> Unit,
        vararg children: VDOMNode<*, *>
    ): VDOMNode<FrameLayout, ViewAttributes> {
        return VDOMNode(
            FrameLayout::class.java,
            ViewAttributes().apply(config),
            children = children.toList(),
            create = { context -> FrameLayout(context) },
            update = { _, _ -> }
        )
    }

    open class LinearLayoutAttributes : ViewAttributes() {
        var orientation: Int = LinearLayout.HORIZONTAL
    }

    fun LinearLayout(
        vararg children: VDOMNode<*, *>,
    ): VDOMNode<LinearLayout, LinearLayoutAttributes> {
        return LinearLayout({}, *children)
    }

    fun LinearLayout(
        config: LinearLayoutAttributes.() -> Unit,
        vararg children: VDOMNode<*, *>,
    ): VDOMNode<LinearLayout, LinearLayoutAttributes> {
        val attributes = LinearLayoutAttributes().apply(config)
        return VDOMNode(
            LinearLayout::class.java,
            attributes,
            children = children.toList(),
            create = ::LinearLayout,
            update = { view, attrs ->
                if (view.orientation != attrs.orientation) {
                    view.orientation = attrs.orientation
                }
            }
        )
    }

    fun Column(
        vararg children: VDOMNode<*, *>
    ): VDOMNode<LinearLayout, LinearLayoutAttributes> {
        return Column({}, *children)
    }

    fun Column(
        config: ViewAttributes.() -> Unit,
        vararg children: VDOMNode<*, *>
    ): VDOMNode<LinearLayout, LinearLayoutAttributes> {
        return LinearLayout({
            config()
            orientation = LinearLayout.VERTICAL
        }, *children)
    }

    fun Row(
        vararg children: VDOMNode<*, *>
    ): VDOMNode<LinearLayout, LinearLayoutAttributes> {
        return Row({}, *children)
    }

    fun Row(
        config: ViewAttributes.() -> Unit,
        vararg children: VDOMNode<*, *>
    ): VDOMNode<LinearLayout, LinearLayoutAttributes> {
        return LinearLayout({
            config()
            orientation = LinearLayout.HORIZONTAL
        }, *children)
    }

    open class EditTextAttributes : TextAttributes() {
        /**
         * Determines whether to update the text based on the text input. This option will be removed once edit texts have smoothing updating with this framework.
         */
        var updateText: Boolean = false
        var hint: String? = null
        var onTextChanged: TextWatcher? = null
    }

    fun EditText(
        config: EditTextAttributes.() -> Unit
    ): VDOMNode<ReactiveEditText, EditTextAttributes> {
        val attributes = EditTextAttributes().apply(config)
        return VDOMNode(
            ReactiveEditText::class.java,
            attributes,
            create = ::ReactiveEditText,
            update = { view, attrs ->
                if (view.hint != attrs.hint) {
                    view.hint = attrs.hint
                }

                if ((attrs.updateText || view.text == null) && view.text != attrs.text) {
                    view.setText(attrs.text)
                }

                view.setTextChangedListener(attrs.onTextChanged)
            }
        )
    }

    open class TextAttributes : ViewAttributes() {
        var text: CharSequence? = null
    }

    fun Text(
        config: TextAttributes.() -> Unit = {}
    ): VDOMNode<TextView, TextAttributes> {
        val attributes = TextAttributes().apply(config)
        return VDOMNode(
            TextView::class.java,
            attributes,
            create = ::TextView,
            update = { view, attrs ->
                if (view.text != attrs.text) {
                    view.text = attrs.text
                }
            }
        )
    }

    open class ButtonAttributes : TextAttributes()

    fun Button(
        config: ButtonAttributes.() -> Unit
    ): VDOMNode<Button, ButtonAttributes> {
        val attributes = ButtonAttributes().apply(config)
        return VDOMNode(
            Button::class.java,
            attributes,
            create = ::Button,
            update = { view, attrs ->
                if (view.text != attrs.text) {
                    view.text = attrs.text
                }
            }
        )
    }

    open class ImageAttributes : ViewAttributes() {
        var icon: Icon? = null
    }

    fun Image(
        config: ImageAttributes.() -> Unit
    ): VDOMNode<ReactiveImageView, ImageAttributes> {
        val attributes = ImageAttributes().apply(config)
        return VDOMNode(
            ReactiveImageView::class.java,
            attributes,
            create = ::ReactiveImageView,
            update = { view, attrs ->
                view.setIcon(attrs.icon)
            }
        )
    }

    open class ImageButtonAttributes : ImageAttributes()

    fun ImageButton(
        config: ImageButtonAttributes.() -> Unit
    ): VDOMNode<ReactiveImageButtonView, ImageAttributes> {
        val attributes = ImageButtonAttributes().apply(config)
        return VDOMNode(
            ReactiveImageButtonView::class.java,
            attributes,
            create = ::ReactiveImageButtonView,
            update = { view, attrs ->
                view.setIcon(attrs.icon)
            }
        )
    }

    open class AndromedaListAttributes : ViewAttributes() {
        var items: List<ListItem> = emptyList()
    }

    fun AndromedaList(
        config: AndromedaListAttributes.() -> Unit,
    ): VDOMNode<AndromedaListView, AndromedaListAttributes> {
        val attributes = AndromedaListAttributes().apply(config)
        return VDOMNode(
            AndromedaListView::class.java,
            attributes,
            create = { context ->
                AndromedaListView(context, null)
            },
            update = { view, attrs ->
                if (view.items != attrs.items) {
                    view.setItems(attrs.items)
                }
            }
        )
    }

    fun Component(
        rerenderWhenParentRerenders: Boolean = true,
        onUpdate: ReactiveComponent.(attributes: ViewAttributes) -> VDOMNode<*, *>
    ): VDOMNode<ReactiveComponentView<ViewAttributes>, ViewAttributes> {
        @Suppress("UNCHECKED_CAST")
        return VDOMNode(
            ReactiveComponentView::class.java as Class<ReactiveComponentView<ViewAttributes>>,
            ViewAttributes(),
            managesOwnChildren = true,
            create = { context ->
                ReactiveComponentView(
                    context,
                    rerenderWhenParentRerenders,
                    onUpdate
                )
            },
            update = { view, attributes ->
                view.onUpdate(attributes)
            }
        )
    }

    inline fun <reified T : ViewAttributes> Component(
        config: T.() -> Unit,
        rerenderWhenParentRerenders: Boolean = true,
        noinline onUpdate: ReactiveComponent.(attributes: T) -> VDOMNode<*, *>
    ): VDOMNode<ReactiveComponentView<T>, T> {
        @Suppress("UNCHECKED_CAST")
        return VDOMNode(
            ReactiveComponentView::class.java as Class<ReactiveComponentView<T>>,
            T::class.java.getConstructor().newInstance().apply(config),
            managesOwnChildren = true,
            create = { context ->
                ReactiveComponentView(
                    context,
                    rerenderWhenParentRerenders,
                    onUpdate
                )
            },
            update = { view, attributes ->
                view.onUpdate(attributes)
            }
        )
    }
}
