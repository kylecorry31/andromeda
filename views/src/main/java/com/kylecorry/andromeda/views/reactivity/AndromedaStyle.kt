package com.kylecorry.andromeda.views.reactivity

import android.view.ViewGroup

open class AndromedaStyle(
    val paddingStart: Int = 0,
    val paddingTop: Int = 0,
    val paddingEnd: Int = 0,
    val paddingBottom: Int = 0,
    val marginStart: Int = 0,
    val marginTop: Int = 0,
    val marginEnd: Int = 0,
    val marginBottom: Int = 0,
    val width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
)