package com.kylecorry.andromeda.bitmaps.operations

import android.graphics.Bitmap
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

    constructor(
        bounds: PixelBounds,
        @ColorInt backgroundColor: Int? = null,
        maxSize: Size? = null
    ) {
        this.bounds = bounds
        this.percentBounds = null
        this.backgroundColor = backgroundColor
        this.maxSize = maxSize
    }

    constructor(
        bounds: PercentBounds, @ColorInt backgroundColor: Int? = null,
        maxSize: Size? = null
    ) {
        this.bounds = null
        this.percentBounds = bounds
        this.backgroundColor = backgroundColor
        this.maxSize = maxSize
    }

    override fun execute(bitmap: Bitmap): Bitmap {
        val actualBounds = percentBounds?.toPixelBounds(
            bitmap.width.toFloat(),
            bitmap.height.toFloat()
        ) ?: bounds ?: return bitmap

        return bitmap.fixPerspective(
            actualBounds,
            backgroundColor = backgroundColor,
            maxOutputSize = maxSize
        )
    }
}