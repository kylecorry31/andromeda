package com.kylecorry.andromeda.core.system

import android.content.Context
import android.view.View

object ViewMeasurementUtils {

    fun heightCm(view: View): Float {
        return heightIn(view) * 2.54f
    }

    fun heightIn(view: View): Float {
        val dpi = ydpi(view.context)
        return view.height / dpi
    }

    fun widthCm(view: View): Float {
        return widthIn(view) * 2.54f
    }

    fun widthIn(view: View): Float {
        val dpi = xdpi(view.context)
        return view.width / dpi
    }

    fun dpi(context: Context): Float {
        return context.resources.displayMetrics.densityDpi.toFloat()
    }

    fun ydpi(context: Context): Float {
        return context.resources.displayMetrics.ydpi
    }

    fun xdpi(context: Context): Float {
        return context.resources.displayMetrics.xdpi
    }

    fun density(context: Context): Float {
        return context.resources.displayMetrics.density
    }

}