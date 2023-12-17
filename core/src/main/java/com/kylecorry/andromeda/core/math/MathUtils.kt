package com.kylecorry.andromeda.core.math

import android.util.Size

object MathUtils {

    /**
     * Scales the original size to fit within the max size while maintaining the aspect ratio
     * @param original the original size
     * @param maxSize the max size
     * @return the scaled size
     */
    fun scaleToBounds(original: Size, maxSize: Size): Size {
        val ratioOriginal = original.width.toFloat() / original.height.toFloat()
        val ratioMax = maxSize.width.toFloat() / maxSize.height.toFloat()
        var finalWidth = maxSize.width
        var finalHeight = maxSize.height
        if (ratioMax > ratioOriginal) {
            finalWidth = (maxSize.height.toFloat() * ratioOriginal).toInt()
        } else {
            finalHeight = (maxSize.width.toFloat() / ratioOriginal).toInt()
        }
        return Size(finalWidth, finalHeight)
    }

}