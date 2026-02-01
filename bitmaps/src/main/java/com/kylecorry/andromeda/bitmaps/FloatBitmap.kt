package com.kylecorry.andromeda.bitmaps

class FloatBitmap(val width: Int, val height: Int, val channels: Int) {
    val data = FloatArray(width * height * channels)

    fun get(x: Int, y: Int, channel: Int): Float {
        return data[(y * width + x) * channels + channel]
    }

    fun set(x: Int, y: Int, channel: Int, value: Float) {
        data[(y * width + x) * channels + channel] = value
    }

    fun getOrNull(x: Int, y: Int, channel: Int): Float? {
        if (x !in 0 until width || y !in 0 until height || channel !in 0 until channels) {
            return null
        }
        return get(x, y, channel)
    }

    fun getX(index: Int): Int {
        return index % width
    }

    fun getY(index: Int): Int {
        return index / width
    }

    fun upscale(newWidth: Int, newHeight: Int, trim: Int = 0): FloatBitmap {
        val output = FloatBitmap(newWidth, newHeight, channels)
        val t = trim.toFloat()
        val result = Toolkit.interpolateFloatBitmap(
            data,
            width,
            height,
            channels,
            newWidth,
            newHeight,
            srcStartX = t,
            srcStartY = t,
            srcEndX = width - 1 - t,
            srcEndY = height - 1 - t,
            maxSearchRadius = trim.coerceAtMost(2)
        )
        result.copyInto(output.data)
        return output
    }
}