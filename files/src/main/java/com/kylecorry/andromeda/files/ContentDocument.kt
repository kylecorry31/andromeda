package com.kylecorry.andromeda.files

import android.net.Uri
import java.time.Instant

data class ContentDocument(
    val uri: Uri,
    val displayName: String?,
    val mimeType: String?,
    val canRead: Boolean,
    val canWrite: Boolean,
    val lastModified: Instant,
    val size: Long
)