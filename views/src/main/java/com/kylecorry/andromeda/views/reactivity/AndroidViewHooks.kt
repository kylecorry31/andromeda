package com.kylecorry.andromeda.views.reactivity

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.kylecorry.andromeda.core.ui.ReactiveComponent

object AndroidViewHooks {

    fun ReactiveComponent.useViewAttributes(view: View, attributes: ViewAttributes) {
        useEffect(view) {
            if (view.layoutParams == null) {
                view.layoutParams =
                    ViewGroup.MarginLayoutParams(attributes.width, attributes.height)
            }
            view.id = View.generateViewId()
        }

        useEffect(view, attributes.visibility) {
            view.visibility = attributes.visibility
        }

        useEffect(
            view,
            attributes.paddingStart,
            attributes.paddingTop,
            attributes.paddingEnd,
            attributes.paddingBottom
        ) {
            view.setPadding(
                attributes.paddingStart,
                attributes.paddingTop,
                attributes.paddingEnd,
                attributes.paddingBottom
            )
        }

        useEffect(
            view,
            attributes.marginStart,
            attributes.marginTop,
            attributes.marginEnd,
            attributes.marginBottom
        ) {
            val params = view.layoutParams as? ViewGroup.MarginLayoutParams
            params?.setMargins(
                attributes.marginStart,
                attributes.marginTop,
                attributes.marginEnd,
                attributes.marginBottom
            )
        }

        useEffect(view, attributes.width) {
            view.layoutParams.width = attributes.width
        }

        useEffect(view, attributes.height) {
            view.layoutParams.height = attributes.height
        }

        useEffect(view, attributes.onClick) {
            if (attributes.onClick == null) {
                view.setOnClickListener(null)
            } else {
                view.setOnClickListener {
                    attributes.onClick?.invoke()
                }
            }
        }

        useEffect(view, attributes.onLongClick) {
            if (attributes.onLongClick == null) {
                view.setOnLongClickListener(null)
            } else {
                view.setOnLongClickListener {
                    attributes.onLongClick?.invoke() ?: false
                }
            }
        }
    }

    fun <T : View> ReactiveComponent.useAndroidView(
        attributes: ViewAttributes = ViewAttributes(),
        create: (context: Context) -> T
    ): T {
        val context = useAndroidContext()
        val view = useMemo(context) {
            create(context)
        }

        useViewAttributes(view, attributes)

        return view
    }

    fun <T : ViewGroup> ReactiveComponent.useAndroidViewGroup(
        attributes: ViewGroupAttributes = ViewGroupAttributes(),
        create: (context: Context) -> T
    ): T {
        val context = useAndroidContext()
        val view = useMemo(context) {
            create(context)
        }

        useViewAttributes(view, attributes)

        useEffect(view, attributes.children.joinToString { it.id.toString() }) {
            view.removeAllViews()
            attributes.children.forEach {
                view.addView(it)
            }
        }

        return view
    }
}