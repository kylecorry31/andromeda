package com.kylecorry.andromeda.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

object ExternalFiles {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun read(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val inputStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                return@withContext null
            }

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
    suspend fun write(context: Context, uri: Uri, text: String): Boolean {
        return withContext(Dispatchers.IO) {
            val outputStream = try {
                context.contentResolver.openOutputStream(uri)
            } catch (e: Exception) {
                return@withContext false
            }

            try {
                outputStream?.write(text.toByteArray())
                true
            } catch (e: Exception) {
                false
            } finally {
                outputStream?.close()
            }
        }
    }
}