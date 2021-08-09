package com.kylecorry.andromeda.core.math

class MovingAverageFilter(var size: Int) {

    private val window = mutableListOf<Double>()

    fun filter(measurement: Double): Double {
        window.add(measurement)
        if (window.size > size){
            window.removeAt(0)
        }
        return window.average()
    }
}