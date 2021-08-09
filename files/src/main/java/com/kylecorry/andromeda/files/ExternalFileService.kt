package com.kylecorry.andromeda.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class ExternalFileService(private val context: Context) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun read(uri: Uri): String? {
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
    suspend fun write(uri: Uri, text: String): Boolean {
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

    fun createFile(filename: String, type: String): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        intent.putExtra(Intent.EXTRA_TITLE, filename)
        return intent
    }

    fun pickFile(type: String, message: String): Intent {
        val requestFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        requestFileIntent.type = type
        return Intent.createChooser(requestFileIntent, message)
    }

    fun pickFile(types: List<String>, message: String): Intent {
        val requestFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        requestFileIntent.type = "*/*"
        requestFileIntent.putExtra(Intent.EXTRA_MIME_TYPES, types.toTypedArray())
        return Intent.createChooser(requestFileIntent, message)
    }

}