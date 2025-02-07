package com.kylecorry.andromeda.views.reactivity

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
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
        updateAttributes(newDom, node.attributes)


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

    private fun updateAttributes(
        dom: View,
        attributes: ViewAttributes
    ) {
        if (dom.layoutParams.width != attributes.width || dom.layoutParams.height != attributes.height) {
            dom.updateLayoutParams<ViewGroup.LayoutParams> {
                width = attributes.width
                height = attributes.height
            }
        }

        if (dom.visibility != attributes.visibility) {
            dom.visibility = attributes.visibility
        }

        if (dom.paddingStart != attributes.paddingStart || dom.paddingEnd != attributes.paddingEnd || dom.paddingTop != attributes.paddingTop || dom.paddingBottom != attributes.paddingBottom) {
            dom.setPadding(
                attributes.paddingStart,
                attributes.paddingTop,
                attributes.paddingEnd,
                attributes.paddingBottom
            )
        }

        if (dom.marginStart != attributes.marginStart || dom.marginEnd != attributes.marginEnd || dom.marginTop != attributes.marginTop || dom.marginBottom != attributes.marginBottom) {
            (dom.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(
                attributes.marginStart,
                attributes.marginTop,
                attributes.marginEnd,
                attributes.marginBottom
            )
        }

        if (dom.width != attributes.width) {
            dom.layoutParams.width = attributes.width
        }

        if (dom.height != attributes.height) {
            dom.layoutParams.height = attributes.height
        }

        if (dom.tag != attributes.tag) {
            dom.tag = attributes.tag
        }

        // Update layout specific parameters
        val params = dom.layoutParams
        when (params) {
            is FrameLayout.LayoutParams -> {
                if (params.gravity != attributes.layoutGravity) {
                    params.gravity = attributes.layoutGravity
                }
            }

            is LinearLayout.LayoutParams -> {
                if (params.gravity != attributes.layoutGravity) {
                    params.gravity = attributes.layoutGravity
                }
            }

            is RelativeLayout.LayoutParams -> {
                // Do nothing
            }

            is ConstraintLayout.LayoutParams -> {
                // Do nothing
            }

            is GridLayout.LayoutParams -> {
                params.setGravity(attributes.layoutGravity)
            }

            else -> {
                // Do nothing
            }
        }

        dom.setOnClickListener(attributes.onClick)
        dom.setOnLongClickListener(attributes.onLongClick)
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

                is GridLayout -> GridLayout.LayoutParams(
                    ViewGroup.LayoutParams(
                        node.attributes.width,
                        node.attributes.height
                    )
                )

                else -> ViewGroup.LayoutParams(node.attributes.width, node.attributes.height)
            }

        newDom.layoutParams = layoutParams
        newDom.id = View.generateViewId()
        return newDom
    }
}

