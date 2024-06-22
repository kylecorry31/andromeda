package com.kylecorry.andromeda.core.ui.colormaps

import com.kylecorry.andromeda.core.ui.Colors
import kotlin.math.min

open class RgbInterpolationColorMap(private val colors: Array<Int>) : ColorMap {
    override fun getColor(percent: Float): Int {
        if (colors.isEmpty()) {
            return 0
        }
        if (colors.size == 1) {
            return colors[0]
        }
        val index = (percent * (colors.size - 1)).toInt()
        val start = colors[index]
        val end = colors[min(index + 1, colors.size - 1)]
        val startPercent = index.toFloat() / (colors.size - 1)
        val endPercent = (index + 1).toFloat() / (colors.size - 1)
        val percentInColor = (percent - startPercent) / (endPercent - startPercent)
        return Colors.interpolate(start, end, percentInColor)
    }
}