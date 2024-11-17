package com.kylecorry.andromeda.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.kylecorry.andromeda.core.system.Resources

abstract class AndromedaWidget(private val themeToReload: Int? = null) : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (themeToReload != null) {
            Resources.reloadTheme(context, themeToReload)
        }

        val views = getUpdatedRemoteViews(context, appWidgetManager)
        if (views != null) {
            for (appWidgetId in appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    /**
     * Get the updated remote views for the widget. This will be passed into appWidgetManager.updateAppWidget for all instances of the widget.
     * @param context The context
     * @param appWidgetManager The app widget manager
     * @return The updated remote views or null if the widget should not be updated
     */
    protected abstract fun getUpdatedRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager
    ): RemoteViews?
}