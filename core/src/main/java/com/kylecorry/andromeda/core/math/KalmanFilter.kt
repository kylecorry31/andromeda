package com.kylecorry.andromeda.core.math

class KalmanFilter(
    initialEstimate: Double,
    private val initialError: Double,
    private val processError: Double,
    initialTime: Double? = null
) {

    private var estimate = initialEstimate
    private var estimateError = initialError
    private var lastTime = initialTime

    fun filter(measurement: Double, error: Double = initialError, time: Double? = null): Double {

        val timeScale = if (lastTime != null && time != null){
            time - lastTime!!
        } else {
            1.0
        }

        lastTime = time

        var divisor = (estimateError + error)
        if (divisor == 0.0) {
            divisor = 0.0001
        }

        val kg = estimateError / divisor
        estimate += kg * (measurement - estimate)
        estimateError = (1 - kg) * (estimateError + processError * timeScale)
        return estimate
    }

    companion object {
        fun filter(
            measurements: List<Double>,
            errors: List<Double>,
            processError: Double,
            times: List<Double>? = null
        ): List<Double> {
            if (measurements.isEmpty()) {
                return emptyList()
            } else if (measurements.size < 2) {
                return listOf(measurements[0])
            }

            val kalman = KalmanFilter(measurements[0], errors[0], processError, times?.get(0))
            val values = mutableListOf(measurements[0])

            for (i in 1..measurements.lastIndex) {
                values.add(kalman.filter(measurements[i], errors[i], times?.get(i)))
            }

            return values
        }

        fun filter(measurements: List<Double>, error: Double, processError: Double, times: List<Double>? = null): List<Double> {
            return filter(measurements, List(measurements.size) { error }, processError, times)
        }

    }

}