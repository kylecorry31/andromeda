package com.kylecorry.andromeda.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes

open class XMLView(@LayoutRes layoutId: Int, context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {
    init {
        inflate(context, layoutId, this)
    }
}