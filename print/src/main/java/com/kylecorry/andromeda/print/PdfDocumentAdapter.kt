package com.kylecorry.andromeda.print

import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import androidx.print.PrintHelper
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

internal class PdfDocumentAdapter(private val uri: Uri, private val callback: PrintHelper.OnPrintFinishCallback) :
    PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        bundle: Bundle
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        } else {
            val builder =
                PrintDocumentInfo.Builder(
                    uri.lastPathSegment ?: "${UUID.randomUUID()}.pdf"
                )
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()

            callback.onLayoutFinished(builder.build(), newAttributes == oldAttributes)
        }
    }

    override fun onWrite(
        pageRanges: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        try {
            // copy file from the input stream to the output stream
            FileInputStream(uri.path).use { inStream ->
                FileOutputStream(destination.fileDescriptor).use { outStream ->
                    inStream.copyTo(outStream)
                }
            }

            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
            } else {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }

        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        }
    }

    override fun onFinish() {
        super.onFinish()
        callback.onFinish()
    }
}