package com.kylecorry.andromeda.core.ui.colormaps

class BinnedColorMap(private val colors: List<Int>) : ColorMap {
    override fun getColor(percent: Float): Int {
        if (colors.isEmpty()) {
            return 0
        }
        if (colors.size == 1) {
            return colors[0]
        }
        val index = (percent * (colors.size - 1)).toInt()
        return colors[index]
    }
}