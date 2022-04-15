package com.kylecorry.andromeda.core.system

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.Size
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService

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

    @Suppress("DEPRECATION")
    fun setShowWhenLocked(activity: Activity, showWhenLocked: Boolean) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                activity.setShowWhenLocked(showWhenLocked)
            }
            showWhenLocked -> {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            }
            else -> {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
        }
    }

    fun getWindowHeight(context: Context): Int {
        return getWindowSize(context).height
    }

    fun getWindowWidth(context: Context): Int {
        return getWindowSize(context).width
    }

    fun getWindowSize(context: Context): Size {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindowSizeSDK30(context)
        } else {
            getWindowSizeLegacy(context)
        }
    }

    @Suppress("DEPRECATION")
    private fun getWindowSizeLegacy(context: Context): Size {
        val window = context.getSystemService<WindowManager>()!!
        val point = Point()
        window.defaultDisplay.getSize(point)
        return Size(point.x, point.y)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getWindowSizeSDK30(context: Context): Size {
        val window = context.getSystemService<WindowManager>()!!
        val metrics = window.currentWindowMetrics
        val windowInsets = metrics.windowInsets
        val insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.displayCutout()
        )

        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        val bounds = metrics.bounds
        return Size(
            bounds.width() - insetsWidth,
            bounds.height() - insetsHeight
        )
    }
}