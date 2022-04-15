package com.kylecorry.andromeda.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

object ExternalFiles {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun read(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val inputStream = stream(context, uri)

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
    suspend fun stream(context: Context, uri: Uri): InputStream? {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun write(context: Context, uri: Uri, text: String): Boolean {
        return withContext(Dispatchers.IO) {
            val stream = outputStream(context, uri) ?: return@withContext false

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
    suspend fun outputStream(context: Context, uri: Uri): OutputStream? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)
            } catch (e: Exception) {
                null
            }
        }
    }
}