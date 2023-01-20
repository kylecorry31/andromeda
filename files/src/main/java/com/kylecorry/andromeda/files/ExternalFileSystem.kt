package com.kylecorry.andromeda.files

import android.content.Context
import android.net.Uri
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
                stream.write(text.toByteArray())
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

    fun getFileName(uri: Uri, withExtension: Boolean = true): String? {
        val name = uri.lastPathSegment ?: return null
        return if (withExtension) {
            name
        } else {
            name.substringBeforeLast(".")
        }
    }

    fun getExtension(uri: Uri): String? {
        val name = getFileName(uri, true) ?: return null
        return name.substringAfterLast(".")
    }
}