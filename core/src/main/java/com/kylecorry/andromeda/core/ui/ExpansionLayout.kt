package com.kylecorry.andromeda.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible

/**
 * An expandable panel view / accordion view.
 * This view can only have two children, the first is the header and the second is the body.
 * Tapping the header will expand or collapse the body.
 */
class ExpansionLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var _header: View? = null
    private var _body: View? = null
    private var _onExpandStateChanged: ((isExpanded: Boolean) -> Unit)? = null

    /**
     * The header view
     */
    var header: View?
        get() = _header
        set(value) {
            _header = value
            _header?.setOnClickListener { toggle() }
            // Replace the first child of the container with the header view
            removeViewAt(0)
            addView(value, 0)
        }

    /**
     * The body view
     */
    var body: View?
        get() = _body
        set(value) {
            val wasExpanded = isExpanded()
            _body = value
            _body?.isVisible = wasExpanded
            // Replace the second child of the container with the body view
            removeViewAt(1)
            addView(value, 1)
        }

    init {
        orientation = VERTICAL
    }

    /**
     * Expand the panel
     */
    fun expand() {
        _body?.isVisible = true
        _onExpandStateChanged?.invoke(isExpanded())
    }

    /**
     * Collapse the panel
     */
    fun collapse() {
        _body?.isVisible = false
        _onExpandStateChanged?.invoke(isExpanded())
    }

    /**
     * Toggle the panel
     */
    fun toggle() {
        if (isExpanded()) {
            collapse()
        } else {
            expand()
        }
    }

    /**
     * @return true if the panel is expanded
     */
    fun isExpanded(): Boolean {
        return _body?.isVisible ?: false
    }

    /**
     * Set a listener for when the panel is expanded or collapsed
     * @param listener the listener to call when the panel is expanded or collapsed
     */
    fun setOnExpandStateChangedListener(listener: (isExpanded: Boolean) -> Unit) {
        _onExpandStateChanged = listener
    }


    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (_header == null) {
            _header = child
            _header?.setOnClickListener {
                toggle()
            }
        } else if (_body == null && child != _header) {
            _body = child
            collapse()
        } else if (child != _header && child != _body) {
            throw IllegalStateException("ExpansionPanelView can only have two children")
        }
        super.addView(child, index, params)
    }
}