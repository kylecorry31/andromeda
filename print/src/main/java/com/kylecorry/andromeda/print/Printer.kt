package com.kylecorry.andromeda.print

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.getSystemService
import androidx.print.PrintHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

class Printer(context: Context) {

    private val helper = PrintHelper(context)
    private val printManager = context.getSystemService<PrintManager>()
    private val attributesBuilder = PrintAttributes.Builder()

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
        attributesBuilder.setColorMode(
            when (colorMode) {
                ColorMode.Color -> PrintAttributes.COLOR_MODE_COLOR
                ColorMode.Monochrome -> PrintAttributes.COLOR_MODE_MONOCHROME
            }
        )
    }

    fun setOrientation(orientation: Orientation) {
        helper.orientation = when (orientation) {
            Orientation.Portrait -> PrintHelper.ORIENTATION_PORTRAIT
            Orientation.Landscape -> PrintHelper.ORIENTATION_LANDSCAPE
        }
        attributesBuilder.setMediaSize(
            when (orientation) {
                Orientation.Portrait -> PrintAttributes.MediaSize.UNKNOWN_PORTRAIT
                Orientation.Landscape -> PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE
            }
        )
    }

    fun print(bitmap: Bitmap, onFinished: (String) -> Unit = {}): String {
        val jobName = createJobName()
        helper.printBitmap(jobName, bitmap) { onFinished(jobName) }
        return jobName
    }

    fun print(uri: Uri, onFinished: (String) -> Unit = {}): String {
        val jobName = createJobName()
        // If it is a PDF, print it using the print manager
        if (uri.toString().endsWith(".pdf", true)) {
            printManager?.print(
                jobName,
                PdfDocumentAdapter(uri) { onFinished(jobName) },
                attributesBuilder.build()
            )
        } else {
            helper.printBitmap(jobName, uri) { onFinished(jobName) }
        }
        return jobName
    }

    suspend fun print(uri: Uri): String = suspendCancellableCoroutine { cont ->
        print(uri) {
            cont.resume(it)
        }
    }

    suspend fun print(bitmap: Bitmap): String = suspendCancellableCoroutine { cont ->
        print(bitmap) {
            cont.resume(it)
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