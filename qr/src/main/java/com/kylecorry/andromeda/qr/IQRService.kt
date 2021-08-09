package com.kylecorry.andromeda.qr

import android.graphics.Bitmap

interface IQRService {

    fun encode(text: String, width: Int, height: Int, errorCorrection: QRErrorCorrection = QRErrorCorrection.M): Bitmap

    fun decode(image: Bitmap): String?

}