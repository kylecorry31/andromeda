package com.kylecorry.andromeda.core.math

import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {

    private val array = floatArrayOf(x, y, z)

    fun cross(other: Vector3): Vector3 {
        val arr = Vector3Utils.cross(toFloatArray(), other.toFloatArray())
        return Vector3(arr[0], arr[1], arr[2])
    }

    operator fun minus(other: Vector3): Vector3 {
        val arr = Vector3Utils.minus(toFloatArray(), other.toFloatArray())
        return Vector3(arr[0], arr[1], arr[2])
    }

    operator fun plus(other: Vector3): Vector3 {
        val arr = Vector3Utils.plus(toFloatArray(), other.toFloatArray())
        return Vector3(arr[0], arr[1], arr[2])
    }

    operator fun times(factor: Float): Vector3 {
        val arr = Vector3Utils.times(toFloatArray(), factor)
        return Vector3(arr[0], arr[1], arr[2])
    }

    fun toFloatArray(): FloatArray {
        return array
    }

    fun dot(other: Vector3): Float {
        return Vector3Utils.dot(toFloatArray(), other.toFloatArray())
    }

    fun magnitude(): Float {
        return Vector3Utils.magnitude(toFloatArray())
    }

    fun normalize(): Vector3 {
        val arr = Vector3Utils.normalize(toFloatArray())
        return Vector3(arr[0], arr[1], arr[2])
    }

    companion object {
        val zero = Vector3(0f, 0f, 0f)

        fun from(arr: FloatArray): Vector3 {
            return Vector3(arr[0], arr[1], arr[2])
        }

    }

}


object Vector3Utils {
    fun cross(first: FloatArray, second: FloatArray): FloatArray {
        return floatArrayOf(
            first[1] * second[2] - first[2] * second[1],
            first[2] * second[0] - first[0] * second[2],
            first[0] * second[1] - first[1] * second[0]
        )
    }

    fun minus(first: FloatArray, second: FloatArray): FloatArray {
        return floatArrayOf(
            first[0] - second[0],
            first[1] - second[1],
            first[2] - second[2]
        )
    }

    fun plus(first: FloatArray, second: FloatArray): FloatArray {
        return floatArrayOf(
            first[0] + second[0],
            first[1] + second[1],
            first[2] + second[2]
        )
    }

    fun times(arr: FloatArray, factor: Float): FloatArray {
        return floatArrayOf(
            arr[0] * factor,
            arr[1] * factor,
            arr[2] * factor
        )
    }

    fun dot(first: FloatArray, second: FloatArray): Float {
        return first[0] * second[0] + first[1] * second[1] + first[2] * second[2]
    }

    fun magnitude(arr: FloatArray): Float {
        return sqrt(arr[0] * arr[0] + arr[1] * arr[1] + arr[2] * arr[2])
    }

    fun normalize(arr: FloatArray): FloatArray {
        val mag = magnitude(arr)
        return floatArrayOf(
            arr[0] / mag,
            arr[1] / mag,
            arr[2] / mag
        )
    }
}