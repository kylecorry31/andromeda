package com.kylecorry.andromeda.core.system

import android.R
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get

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
        return getAndroidColorAttr(context, R.attr.textColorPrimary)
    }

    @ColorInt
    fun androidBackgroundColorPrimary(context: Context): Int {
        return getAndroidColorAttr(context, R.attr.colorBackground)
    }

    @ColorInt
    fun androidBackgroundColorSecondary(context: Context): Int {
        return getAndroidColorAttr(context, R.attr.colorBackgroundFloating)
    }

    @ColorInt
    fun androidTextColorSecondary(context: Context): Int {
        return getAndroidColorAttr(context, R.attr.textColorSecondary)
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
        for (i in 0 until menu.size()) {
            items.add(menu[i])
        }
        return items
    }
}