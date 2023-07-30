package com.kylecorry.andromeda.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.kylecorry.andromeda.core.io.write
import com.kylecorry.andromeda.core.io.writeAll
import com.kylecorry.andromeda.core.tryOrLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

class ExternalFileSystem(private val context: Context) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun read(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val inputStream = stream(uri)

            try {
                inputStream?.bufferedReader()?.readText()
            } catch (e: Exception) {
                null
            } finally {
                inputStream?.close()
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun stream(uri: Uri): InputStream? {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun write(uri: Uri, text: String): Boolean {
        return withContext(Dispatchers.IO) {
            val stream = outputStream(uri) ?: return@withContext false

            try {
                stream.write(text)
                true
            } catch (e: Exception) {
                false
            } finally {
                stream.close()
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun outputStream(uri: Uri): OutputStream? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    fun getFileName(
        uri: Uri,
        withExtension: Boolean = true,
        fallbackToPath: Boolean = true
    ): String? {

        var name: String? = null
        tryOrLog {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    it.moveToFirst()
                    name = it.getString(idx)
                }
            }
        }

        if (name == null && fallbackToPath) {
            name = uri.lastPathSegment
        }

        return if (withExtension) {
            name
        } else {
            name?.substringBeforeLast(".")
        }
    }

    fun getExtension(uri: Uri): String? {
        val name = getFileName(uri, withExtension = true, fallbackToPath = true) ?: return null
        return name.substringAfterLast(".")
    }
}