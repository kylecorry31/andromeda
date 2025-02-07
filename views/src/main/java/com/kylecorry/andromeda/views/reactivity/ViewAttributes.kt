package com.kylecorry.andromeda.views.reactivity

import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
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
    var tag: String = ""
    var onClick: OnClickListener? = null
    var onLongClick: OnLongClickListener? = null
    // TODO: Gravity
}