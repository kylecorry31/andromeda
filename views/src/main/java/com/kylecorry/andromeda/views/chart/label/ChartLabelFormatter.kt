package com.kylecorry.andromeda.views.chart.label

interface ChartLabelFormatter {
    fun format(value: Float): String
}