package com.kylecorry.andromeda.core.math

import android.util.Range
import kotlin.math.*

fun normalizeAngle(angle: Float): Float {
    return wrap(angle, 0f, 360f) % 360
}

fun normalizeAngle(angle: Double): Double {
    return wrap(angle, 0.0, 360.0) % 360
}

fun wrap(value: Float, min: Float, max: Float): Float {
    return wrap(value.toDouble(), min.toDouble(), max.toDouble()).toFloat()
}

fun wrap(value: Double, min: Double, max: Double): Double {
    val range = max - min

    var newValue = value

    while (newValue > max) {
        newValue -= range
    }

    while (newValue < min) {
        newValue += range
    }

    return newValue
}

fun sinDegrees(angle: Double): Double {
    return sin(angle.toRadians())
}

fun tanDegrees(angle: Double): Double {
    return tan(angle.toRadians())
}

fun tanDegrees(angle: Float): Float {
    return tan(angle.toRadians())
}

fun cosDegrees(angle: Double): Double {
    return cos(angle.toRadians())
}

fun Double.toRadians(): Double {
    return Math.toRadians(this)
}

fun Float.toRadians(): Float {
    return Math.toRadians(this.toDouble()).toFloat()
}

fun deltaAngle(angle1: Float, angle2: Float): Float {
    var delta = angle2 - angle1
    delta += 180
    delta -= floor(delta / 360) * 360
    delta -= 180
    if (abs(abs(delta) - 180) <= Float.MIN_VALUE) {
        delta = 180f
    }
    return delta
}

fun clamp(value: Float, minimum: Float, maximum: Float): Float {
    return min(maximum, max(minimum, value))
}

fun Double.roundPlaces(places: Int): Double {
    return (this * 10.0.pow(places)).roundToInt() / 10.0.pow(places)
}

fun Float.roundPlaces(places: Int): Float {
    return (this * 10f.pow(places)).roundToInt() / 10f.pow(places)
}

fun Float.toDegrees(): Float {
    return Math.toDegrees(this.toDouble()).toFloat()
}

fun Double.toDegrees(): Double {
    return Math.toDegrees(this)
}

fun smooth(data: List<Float>, smoothing: Float = 0.5f): List<Float> {
    if (data.isEmpty()) {
        return data
    }

    val filter = LowPassFilter(smoothing, data.first())

    return data.mapIndexed { index, value ->
        if (index == 0) {
            value
        } else {
            filter.filter(value)
        }
    }
}

fun movingAverage(data: List<Float>, window: Int = 5): List<Float> {
    val filter = MovingAverageFilter(window)

    return data.map { filter.filter(it.toDouble()).toFloat() }
}

/**
 * Calculates the slope of the best fit line
 */
fun slope(data: List<Pair<Float, Float>>): Float {
    if (data.isEmpty()) {
        return 0f
    }

    val xBar = data.map { it.first }.average().toFloat()
    val yBar = data.map { it.second }.average().toFloat()

    var ssxx = 0.0f
    var ssxy = 0.0f
    var ssto = 0.0f

    for (i in data.indices) {
        ssxx += (data[i].first - xBar).pow(2)
        ssxy += (data[i].first - xBar) * (data[i].second - yBar)
        ssto += (data[i].second - yBar).pow(2)
    }

    return ssxy / ssxx
}

fun power(x: Int, power: Int): Int {
    var total = 1
    for (i in 0 until abs(power)) {
        total *= x
    }

    if (power < 0) {
        return 0
    }

    return total
}

fun power(x: Double, power: Int): Double {
    var total = 1.0
    for (i in 0 until abs(power)) {
        total *= x
    }

    if (power < 0) {
        return 1 / total
    }

    return total
}

fun String.toDoubleCompat(): Double? {
    val asDouble = try {
        this.replace(",", ".").toDoubleOrNull()
    } catch (e: Exception) {
        null
    }
    asDouble ?: return null
    if (asDouble.isNaN() || asDouble.isInfinite()) {
        return null
    }
    return asDouble
}

fun List<Double>.toDoubleArray(): DoubleArray {
    return DoubleArray(size) {
        get(it)
    }
}

fun String.toFloatCompat(): Float? {
    val asFloat = try {
        this.replace(",", ".").toFloatOrNull()
    } catch (e: Exception) {
        null
    }
    asFloat ?: return null
    if (asFloat.isNaN() || asFloat.isInfinite()) {
        return null
    }
    return asFloat
}

fun String.toIntCompat(): Int? {
    return try {
        this.replace(",", ".").toIntOrNull()
    } catch (e: Exception) {
        null
    }
}

fun String.toLongCompat(): Long? {
    return try {
        this.replace(",", ".").toLongOrNull()
    } catch (e: Exception) {
        null
    }
}

fun removeOutliers(
    measurements: List<Double>,
    threshold: Double,
    replaceWithAverage: Boolean = false,
    replaceLast: Boolean = false
): List<Double> {
    if (measurements.size < 3) {
        return measurements
    }

    val filtered = mutableListOf(measurements.first())

    for (i in 1 until measurements.lastIndex) {
        val before = measurements[i - 1]
        val current = measurements[i]
        val after = measurements[i + 1]

        val last = if (replaceWithAverage) (before + after) / 2 else filtered.last()

        if (current - before > threshold && current - after > threshold) {
            filtered.add(last)
        } else if (current - before < -threshold && current - after < -threshold) {
            filtered.add(last)
        } else {
            filtered.add(current)
        }
    }

    if (replaceLast && abs(filtered.last() - measurements.last()) > threshold){
        filtered.add(filtered.last())
    } else {
        filtered.add(measurements.last())
    }
    return filtered
}

fun constrain(value: Float, minimum: Float, maximum: Float): Float {
    return value.coerceIn(minimum, maximum)
}

fun lerp(start: Float, end: Float, percent: Float): Float {
    return start + (end - start) * percent
}

fun map(value: Float, originalMin: Float, originalMax: Float, newMin: Float, newMax: Float): Float {
    val normal = norm(value, originalMin, originalMax)
    return lerp(newMin, newMax, normal)
}

fun norm(value: Float, minimum: Float, maximum: Float): Float {
    val range = maximum - minimum
    if (range == 0f){
        return 0f
    }
    return (value - minimum) / range
}

fun scaleToFit(
        width: Float,
        height: Float,
        maxWidth: Float,
        maxHeight: Float
): Float {
    return min(maxWidth / width, maxHeight / height)
}

fun List<Float>.rangeOrNull(): Range<Float>? {
    val min = minOrNull() ?: return null
    val max = maxOrNull() ?: return null
    return Range(min, max)
}