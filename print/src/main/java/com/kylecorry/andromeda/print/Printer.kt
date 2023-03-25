package com.kylecorry.andromeda.print

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.print.PrintHelper
import com.kylecorry.andromeda.core.coroutines.makeSuspend
import java.util.UUID
import kotlin.coroutines.resume

class Printer(context: Context) {

    private val helper = PrintHelper(context)

    fun setScaleMode(scaleMode: ScaleMode) {
        helper.scaleMode = when (scaleMode) {
            ScaleMode.Fit -> PrintHelper.SCALE_MODE_FIT
            ScaleMode.Fill -> PrintHelper.SCALE_MODE_FILL
        }
    }

    fun setColorMode(colorMode: ColorMode) {
        helper.colorMode = when (colorMode) {
            ColorMode.Color -> PrintHelper.COLOR_MODE_COLOR
            ColorMode.Monochrome -> PrintHelper.COLOR_MODE_MONOCHROME
        }
    }

    fun setOrientation(orientation: Orientation) {
        helper.orientation = when (orientation) {
            Orientation.Portrait -> PrintHelper.ORIENTATION_PORTRAIT
            Orientation.Landscape -> PrintHelper.ORIENTATION_LANDSCAPE
        }
    }

    fun print(bitmap: Bitmap, onFinished: (String) -> Unit = {}): String {
        val jobName = createJobName()
        helper.printBitmap(jobName, bitmap) { onFinished(jobName) }
        return jobName
    }

    fun print(uri: Uri, onFinished: (String) -> Unit = {}): String {
        val jobName = createJobName()
        helper.printBitmap(jobName, uri) { onFinished(jobName) }
        return jobName
    }

    suspend fun print(uri: Uri): String {
        return makeSuspend<String> { cont ->
            print(uri) {
                cont.resume(it)
            }
        }
    }

    suspend fun print(bitmap: Bitmap): String {
        return makeSuspend<String> { cont ->
            print(bitmap) {
                cont.resume(it)
            }
        }
    }

    private fun createJobName(): String {
        val guid = UUID.randomUUID().toString()
        return "PrintJob-$guid"
    }

    companion object {
        fun canPrint(): Boolean {
            return PrintHelper.systemSupportsPrint()
        }
    }

}