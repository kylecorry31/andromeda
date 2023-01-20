package com.kylecorry.andromeda.files

import android.webkit.MimeTypeMap

object MimeType {

    fun toExtension(mimeType: String): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    fun toMimeType(extension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

}