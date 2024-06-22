package com.kylecorry.andromeda.core.ui.colormaps

class PrecalculatedColorMap(private val map: ColorMap, private val buckets: Int = 256) : ColorMap {

    private val colors = Array(buckets) { map.getColor(it.toFloat() / (buckets - 1)) }

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