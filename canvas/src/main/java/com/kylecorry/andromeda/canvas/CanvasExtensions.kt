package com.kylecorry.andromeda.canvas

import android.graphics.*
import androidx.core.graphics.createBitmap

inline fun Canvas.getMaskedBitmap(
    mask: Bitmap,
    tempBitmap: Bitmap = createBitmap(width, height),
    block: (canvas: Canvas) -> Unit
): Bitmap {
    val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    val tempCanvas = Canvas(tempBitmap)
    tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.DST_IN)
    block(tempCanvas)
    tempCanvas.drawBitmap(mask, 0f, 0f, maskPaint)
    return tempBitmap
}