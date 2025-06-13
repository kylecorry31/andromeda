package com.kylecorry.andromeda.core.system

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.get
import java.util.Locale
import androidx.core.view.size

object Resources {
    fun dp(context: Context, size: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, size,
            context.resources.displayMetrics
        )
    }

    fun sp(context: Context, size: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, size,
            context.resources.displayMetrics
        )
    }

    @ColorInt
    fun getAndroidColorAttr(context: Context, @AttrRes attrRes: Int): Int {
        val theme = context.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        val colorRes = if (typedValue.resourceId != 0) typedValue.resourceId else typedValue.data
        return ContextCompat.getColor(context, colorRes)
    }

    @ColorInt
    fun androidTextColorPrimary(context: Context): Int {
        return getAndroidColorAttr(context, android.R.attr.textColorPrimary)
    }

    @ColorInt
    fun androidBackgroundColorPrimary(context: Context): Int {
        return getAndroidColorAttr(context, android.R.attr.colorBackground)
    }

    @ColorInt
    fun androidBackgroundColorSecondary(context: Context): Int {
        return getAndroidColorAttr(context, android.R.attr.colorBackgroundFloating)
    }

    @ColorInt
    fun androidTextColorSecondary(context: Context): Int {
        return getAndroidColorAttr(context, android.R.attr.textColorSecondary)
    }

    @ColorInt
    fun color(context: Context, @ColorRes colorId: Int): Int {
        return ResourcesCompat.getColor(context.resources, colorId, null)
    }

    fun drawable(context: Context, @DrawableRes drawableId: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, drawableId, null)
    }

    fun menuItems(context: Context, @MenuRes id: Int): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        val p = PopupMenu(context, null)
        p.menuInflater.inflate(id, p.menu)
        val menu = p.menu
        for (i in 0 until menu.size) {
            items.add(menu[i])
        }
        return items
    }

    fun getLocale(context: Context): Locale {
        val config = context.applicationContext.resources.configuration
        val locales = ConfigurationCompat.getLocales(config)
        return locales.get(0) ?: Locale.getDefault()
    }

    fun isMetricPreferred(context: Context): Boolean {
        val locale = getLocale(context)
        // As of 2023, the US and Liberia are the only countries which heavily use imperial units
        // Myanmar is sometimes viewed as a third, but they use metric for most things and local units for others (which are not supported by my apps)
        return when (locale.country.uppercase()) {
            "US", "LR" -> false
            else -> true
        }
    }

    fun uses24HourClock(context: Context): Boolean {
        return DateFormat.is24HourFormat(context)
    }

    fun getLocalizedContext(context: Context, locale: Locale): Context {
        val currentConfig = context.resources.configuration
        val configCopy = Configuration(currentConfig)
        configCopy.setLocale(locale)
        return context.createConfigurationContext(configCopy)
    }

    fun getLocalizedResources(context: Context, locale: Locale): android.content.res.Resources {
        return getLocalizedContext(context, locale).resources
    }

    fun reloadTheme(context: Context, @StyleRes themeResId: Int) {
        context.theme.applyStyle(themeResId, true)
    }
}