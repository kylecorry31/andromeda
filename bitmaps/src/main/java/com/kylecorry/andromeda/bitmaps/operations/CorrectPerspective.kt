package com.kylecorry.andromeda.bitmaps.operations

import android.graphics.Bitmap
import android.graphics.Paint
import android.util.Size
import androidx.annotation.ColorInt
import com.kylecorry.andromeda.bitmaps.BitmapUtils.fixPerspective
import com.kylecorry.andromeda.core.units.PercentBounds
import com.kylecorry.andromeda.core.units.PixelBounds

class CorrectPerspective : BitmapOperation {

    private var bounds: PixelBounds? = null
    private var percentBounds: PercentBounds? = null
    private val backgroundColor: Int?
    private var maxSize: Size? = null
    private var outputSize: Size? = null

    private val paint = Paint()

    constructor(
        bounds: PixelBounds,
        @ColorInt backgroundColor: Int? = null,
        maxSize: Size? = null,
        outputSize: Size? = null,
        interpolate: Boolean = true
    ) {
        this.bounds = bounds
        this.percentBounds = null
        this.backgroundColor = backgroundColor
        this.maxSize = maxSize
        this.outputSize = outputSize
        paint.isFilterBitmap = interpolate
    }

    constructor(
        bounds: PercentBounds,
        @ColorInt backgroundColor: Int? = null,
        maxSize: Size? = null,
        outputSize: Size? = null,
        interpolate: Boolean = true
    ) {
        this.bounds = null
        this.percentBounds = bounds
        this.backgroundColor = backgroundColor
        this.maxSize = maxSize
        this.outputSize = outputSize
        paint.isFilterBitmap = interpolate
    }

    override fun execute(bitmap: Bitmap): Bitmap {
        val actualBounds = percentBounds?.toPixelBounds(
            bitmap.width.toFloat(),
            bitmap.height.toFloat()
        ) ?: bounds ?: return bitmap

        return bitmap.fixPerspective(
            actualBounds,
            backgroundColor = backgroundColor,
            maxOutputSize = maxSize,
            outputSize = outputSize,
            paint = paint
        )
    }
}