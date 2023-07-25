package com.kylecorry.andromeda.core.system

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.util.*

class ResourceCache(private val resources: Resources) {

    private var lastLocale = getLocale()
    private val cache = mutableMapOf<Int, String>()
    private val lock = Any()

    fun getString(id: Int): String {
        synchronized(lock) {
            val currentLocale = getLocale()
            if (lastLocale != currentLocale) {
                cache.clear()
                lastLocale = currentLocale
            }

            if (cache.containsKey(id)) {
                return cache[id]!!
            }
            val string = resources.getString(id)
            cache[id] = string
            return string
        }
    }

    fun getString(id: Int, vararg formatArgs: Any): String {
        val raw = getString(id)
        return String.format(lastLocale, raw, *formatArgs)
    }

    private fun getLocale(): Locale {
        val config = resources.configuration
        val locales = ConfigurationCompat.getLocales(config)
        return locales.get(0) ?: Locale.getDefault()
    }
}