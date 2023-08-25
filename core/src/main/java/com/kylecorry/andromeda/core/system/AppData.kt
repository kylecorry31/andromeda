package com.kylecorry.andromeda.core.system

import android.content.Context
import androidx.core.content.ContextCompat
import java.io.File

object AppData {

    fun getDataDirectory(context: Context): File {
        return ContextCompat.getDataDir(context) ?: getFilesDirectory(context).parentFile!!
    }

    fun getFilesDirectory(context: Context): File {
        return context.filesDir
    }

    fun getCacheDirectory(context: Context): File {
        return context.cacheDir
    }

    fun getDatabaseDirectory(context: Context, databaseName: String? = null): File {
        val lookedUp = if (databaseName != null) {
            context.getDatabasePath(databaseName)?.parentFile
        } else {
            val databases = context.databaseList()
            databases.firstOrNull()?.let {
                context.getDatabasePath(it)?.parentFile
            }
        }

        return lookedUp ?: File(getDataDirectory(context), "databases")
    }

    fun getSharedPrefsDirectory(context: Context): File {
        return File(getDataDirectory(context), "shared_prefs")
    }

    fun getSharedPrefsFile(
        context: Context,
        name: String = "${context.packageName}_preferences"
    ): File {
        return File(getSharedPrefsDirectory(context), "${name}.xml")
    }

    fun getSharedPrefsFiles(context: Context): List<File> {
        return getSharedPrefsDirectory(context).listFiles()?.toList()
            ?.filter { it.extension == "xml" } ?: listOf()
    }

}