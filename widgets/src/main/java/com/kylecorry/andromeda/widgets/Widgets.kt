package com.kylecorry.andromeda.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object Widgets {

    fun requestUpdate(context: Context, component: Class<out AppWidgetProvider>) {
        val intent = Intent(context, component)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(intent.component)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    fun isWidgetInstantiated(context: Context, component: Class<out AppWidgetProvider>): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, component))
        return ids.isNotEmpty()
    }

}