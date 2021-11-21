package com.kylecorry.andromeda.canvas

import android.graphics.*

inline fun Canvas.getMaskedBitmap(
    mask: Bitmap,
    tempBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888),
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