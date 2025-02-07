package com.kylecorry.andromeda.views.reactivity

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams


object VDOM {

    fun render(
        root: ViewGroup,
        node: VDOMNode<*, *>,
        dom: View? = null
    ): View {
        // Update current DOM node
        var newDom = dom
        if (newDom == null) {
            newDom = createDom(root, node)
            root.addView(newDom)
        }

        if (newDom.javaClass != node.domClass) {
            newDom = createDom(root, node)
            val index = root.indexOfChild(dom)
            root.removeView(dom)
            root.addView(newDom, index)
        }

        // Update basic view attributes
        if (newDom.layoutParams.width != node.attributes.width || newDom.layoutParams.height != node.attributes.height) {
            newDom.updateLayoutParams<ViewGroup.LayoutParams> {
                width = node.attributes.width
                height = node.attributes.height
            }
        }

        if (newDom.visibility != node.attributes.visibility) {
            newDom.visibility = node.attributes.visibility
        }

        if (newDom.paddingStart != node.attributes.paddingStart || newDom.paddingEnd != node.attributes.paddingEnd || newDom.paddingTop != node.attributes.paddingTop || newDom.paddingBottom != node.attributes.paddingBottom) {
            newDom.setPadding(
                node.attributes.paddingStart,
                node.attributes.paddingTop,
                node.attributes.paddingEnd,
                node.attributes.paddingBottom
            )
        }

        if (newDom.marginStart != node.attributes.marginStart || newDom.marginEnd != node.attributes.marginEnd || newDom.marginTop != node.attributes.marginTop || newDom.marginBottom != node.attributes.marginBottom) {
            (newDom.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(
                node.attributes.marginStart,
                node.attributes.marginTop,
                node.attributes.marginEnd,
                node.attributes.marginBottom
            )
        }

        if (newDom.width != node.attributes.width) {
            newDom.layoutParams.width = node.attributes.width
        }

        if (newDom.height != node.attributes.height) {
            newDom.layoutParams.height = node.attributes.height
        }

        if (newDom.tag != node.attributes.tag) {
            newDom.tag = node.attributes.tag
        }

        newDom.setOnClickListener(node.attributes.onClick)
        newDom.setOnLongClickListener(node.attributes.onLongClick)

        node.javaClass.getMethod("getUpdate").also {
            @Suppress("UNCHECKED_CAST") val update =
                it.invoke(node) as (View, ViewAttributes) -> Unit
            update(newDom, node.attributes)
        }

        if (node.managesOwnChildren) {
            return newDom
        }

        // Update children
        // TODO: Match children by key if present
        // TODO: Better diffing algorithm
        val nonNullChildren = node.children.filterNotNull()

        nonNullChildren.forEachIndexed { index, child ->
            render(newDom as ViewGroup, child, newDom.children.elementAtOrNull(index))
        }

        // Delete extra children
        if (newDom is ViewGroup) {
            for (i in nonNullChildren.size until newDom.childCount) {
                newDom.removeViewAt(i)
            }
        }

        return newDom
    }

    private fun createDom(
        root: ViewGroup,
        node: VDOMNode<*, *>
    ): View {
        val newDom = node.create(root.context)

        val layoutParams =
            when (root) {
                is FrameLayout -> FrameLayout.LayoutParams(
                    node.attributes.width,
                    node.attributes.height
                )

                is LinearLayout -> LinearLayout.LayoutParams(
                    node.attributes.width,
                    node.attributes.height
                )

                is RelativeLayout -> RelativeLayout.LayoutParams(
                    node.attributes.width,
                    node.attributes.height
                )

                is ConstraintLayout -> ConstraintLayout.LayoutParams(
                    node.attributes.width,
                    node.attributes.height
                )

                else -> ViewGroup.LayoutParams(node.attributes.width, node.attributes.height)
            }

        newDom.layoutParams = layoutParams
        newDom.id = View.generateViewId()
        return newDom
    }
}

