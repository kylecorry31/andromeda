package com.kylecorry.andromeda.views.reactivity

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.kylecorry.andromeda.core.ui.ReactiveComponent

object AndroidViewHooks {

    fun ReactiveComponent.useStyle(view: View, style: AndromedaStyle) {
        useEffect(
            view,
            style.paddingStart,
            style.paddingTop,
            style.paddingEnd,
            style.paddingBottom
        ) {
            view.setPadding(
                style.paddingStart,
                style.paddingTop,
                style.paddingEnd,
                style.paddingBottom
            )
        }

        useEffect(view, style.marginStart, style.marginTop, style.marginEnd, style.marginBottom) {
            val params = view.layoutParams as? ViewGroup.MarginLayoutParams
            params?.setMargins(
                style.marginStart,
                style.marginTop,
                style.marginEnd,
                style.marginBottom
            )
        }

        useEffect(view, style.width) {
            view.layoutParams.width = style.width
        }

        useEffect(view, style.height) {
            view.layoutParams.height = style.height
        }
    }

    fun ReactiveComponent.useLayout(view: ViewGroup, children: Array<out View>) {
        useEffect(view, children.joinToString { it.id.toString() }) {
            view.removeAllViews()
            children.forEach {
                view.addView(it)
            }
        }
    }

    fun <T : View> ReactiveComponent.useAndroidView(create: (context: Context) -> T): T {
        val context = useContext()
        return useMemo(context) {
            create(context)
        }
    }
}