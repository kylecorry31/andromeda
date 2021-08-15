package com.kylecorry.andromeda.core.system

import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager

object Screen {

    fun setKeepScreenOn(window: Window, keepOn: Boolean) {
        if (keepOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun setBrightness(window: Window, brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness.coerceIn(0f, 1f)
        window.attributes = layoutParams
    }

    fun resetBrightness(window: Window) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }

    fun setAllowScreenshots(window: Window, allowed: Boolean) {
        if (allowed) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

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