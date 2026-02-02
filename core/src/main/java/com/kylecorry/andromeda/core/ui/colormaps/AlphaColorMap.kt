package com.kylecorry.andromeda.core.ui.colormaps

import android.graphics.Color
import com.kylecorry.andromeda.core.ui.Colors.withAlpha

class AlphaColorMap(private val baseColor: Int = Color.BLACK, private val maxAlpha: Int = 255) :
    ColorMap {
    override fun getColor(percent: Float): Int {
        val value = percent.coerceIn(0f, 1f) * 255
        val scale = maxAlpha.coerceIn(0, 255) / 255f
        return baseColor.withAlpha((value * scale).toInt())
    }
}