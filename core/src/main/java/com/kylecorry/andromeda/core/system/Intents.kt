package com.kylecorry.andromeda.core.system

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.kylecorry.sol.units.Coordinate

object Intents {

    fun localIntent(context: Context, action: String): Intent {
        val i = Intent(action)
        i.`package` = context.packageName
        i.addCategory(Intent.CATEGORY_DEFAULT)
        return i
    }

    fun startService(context: Context, intent: Intent, foreground: Boolean = false) {
        if (foreground && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun pendingIntentExists(context: Context, requestCode: Int, intent: Intent): Boolean {
        return PendingIntent.getBroadcast(
            context, requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        ) != null
    }

    fun email(to: String, subject: String, body: String = ""): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }

    fun url(url: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
    }

    fun geo(location: Coordinate): Intent {
        return url("geo:${location.latitude},${location.longitude}")
    }

    fun appSettings(context: Context): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", Package.getPackageName(context), null)
        intent.data = uri
        return intent
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun alarmAndReminderSettings(context: Context): Intent {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        val uri = Uri.fromParts("package", Package.getPackageName(context), null)
        intent.data = uri
        return intent
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun batteryOptimizationSettings(): Intent {
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    }

    fun createFile(filename: String, type: String, message: String = filename): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        intent.putExtra(Intent.EXTRA_TITLE, filename)
        return Intent.createChooser(intent, message)
    }

    fun createFile(filename: String, types: List<String>, message: String = filename): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, types.toTypedArray())
        intent.putExtra(Intent.EXTRA_TITLE, filename)
        return Intent.createChooser(intent, message)
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

    fun openChooser(context: Context, intent: Intent, title: String) {
        context.startActivity(Intent.createChooser(intent, title))
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun hasReceiver(context: Context, intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }

}