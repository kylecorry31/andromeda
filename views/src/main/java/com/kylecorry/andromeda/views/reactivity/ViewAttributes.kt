package com.kylecorry.andromeda.views.reactivity

import android.view.ViewGroup

open class ViewAttributes {
    var paddingStart: Int = 0
    var paddingTop: Int = 0
    var paddingEnd: Int = 0
    var paddingBottom: Int = 0
    var marginStart: Int = 0
    var marginTop: Int = 0
    var marginEnd: Int = 0
    var marginBottom: Int = 0
    var width: Int = ViewGroup.LayoutParams.MATCH_PARENT
    var height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    var visibility: Int = ViewGroup.VISIBLE
    var onClick: (() -> Unit)? = null
    var onLongClick: (() -> Boolean)? = null

    fun from(props: ViewAttributes) {
        paddingStart = props.paddingStart
        paddingTop = props.paddingTop
        paddingEnd = props.paddingEnd
        paddingBottom = props.paddingBottom
        marginStart = props.marginStart
        marginTop = props.marginTop
        marginEnd = props.marginEnd
        marginBottom = props.marginBottom
        width = props.width
        height = props.height
        visibility = props.visibility
        onClick = props.onClick
        onLongClick = props.onLongClick
    }

}

inline fun <reified T : ViewAttributes> attributes(block: T.() -> Unit): T {
    val constructor = T::class.java.getConstructor()
    return constructor.newInstance().apply(block)
}

inline fun <reified T : ViewAttributes> attrs(block: T.() -> Unit): T {
    return attributes(block)
}