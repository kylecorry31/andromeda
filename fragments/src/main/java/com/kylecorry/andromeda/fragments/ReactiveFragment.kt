package com.kylecorry.andromeda.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import com.kylecorry.andromeda.fragments.AndromedaFragment
import com.kylecorry.andromeda.views.reactivity.VDOM
import com.kylecorry.andromeda.views.reactivity.VDOMNode

abstract class ReactiveFragment : AndromedaFragment() {

    private val root by lazy { requireView() as ViewGroup }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(requireContext())
    }

    abstract fun render(): VDOMNode<*, *>?

    override fun onUpdate() {
        super.onUpdate()
        renderDOM(render() ?: return)
    }

    private fun renderDOM(node: VDOMNode<*, *>) {
        VDOM.render(root, node, root.children.firstOrNull())
    }
}