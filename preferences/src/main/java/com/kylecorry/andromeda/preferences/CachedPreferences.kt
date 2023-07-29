package com.kylecorry.andromeda.preferences

import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class CachedPreferences(private val preferences: IPreferences) : IPreferences {
    override val onChange: Topic<String> = preferences.onChange

    private val cache = mutableMapOf<String, Any?>()

    init {
        preferences.onChange.subscribe {
            synchronized(cache) {
                cache.remove(it)
            }
            true
        }
    }

    override fun remove(key: String) {
        synchronized(cache) {
            cache.remove(key)
        }
        preferences.remove(key)
    }

    override fun contains(key: String): Boolean {
        val cached = synchronized(cache) {
            cache.containsKey(key)
        }
        return cached || preferences.contains(key)
    }

    override fun putInt(key: String, value: Int) {
        put(key, value, preferences::putInt)
    }

    override fun putBoolean(key: String, value: Boolean) {
        put(key, value, preferences::putBoolean)
    }

    override fun putString(key: String, value: String) {
        put(key, value, preferences::putString)
    }

    override fun putFloat(key: String, value: Float) {
        put(key, value, preferences::putFloat)
    }

    override fun putDouble(key: String, value: Double) {
        put(key, value, preferences::putDouble)
    }

    override fun putLong(key: String, value: Long) {
        put(key, value, preferences::putLong)
    }

    override fun getInt(key: String): Int? {
        return get(key, preferences::getInt)
    }

    override fun getBoolean(key: String): Boolean? {
        return get(key, preferences::getBoolean)
    }

    override fun getString(key: String): String? {
        return get(key, preferences::getString)
    }

    override fun getFloat(key: String): Float? {
        return get(key, preferences::getFloat)
    }

    override fun getDouble(key: String): Double? {
        return get(key, preferences::getDouble)
    }

    override fun getLong(key: String): Long? {
        return get(key, preferences::getLong)
    }

    override fun putCoordinate(key: String, value: Coordinate) {
        put(key, value, preferences::putCoordinate)
    }

    override fun getCoordinate(key: String): Coordinate? {
        return get(key, preferences::getCoordinate)
    }

    override fun getLocalDate(key: String): LocalDate? {
        return get(key, preferences::getLocalDate)
    }

    override fun putLocalDate(key: String, date: LocalDate) {
        put(key, date, preferences::putLocalDate)
    }

    override fun putInstant(key: String, value: Instant) {
        put(key, value, preferences::putInstant)
    }

    override fun getInstant(key: String): Instant? {
        return get(key, preferences::getInstant)
    }

    override fun getDuration(key: String): Duration? {
        return get(key, preferences::getDuration)
    }

    override fun putDuration(key: String, duration: Duration) {
        put(key, duration, preferences::putDuration)
    }

    /**
     * This is not cached
     */
    override fun getAll(): Map<String, *> {
        return preferences.getAll()
    }

    private fun <T> put(key: String, value: T, setter: (String, T) -> Unit) {
        synchronized(cache) {
            cache[key] = value
        }
        setter(key, value)
    }

    private fun <T> get(key: String, getter: (String) -> T?): T? {
        synchronized(cache) {
            if (cache.contains(key)) {
                return cache[key] as T?
            }
        }

        val value = getter(key)
        if (value != null) {
            synchronized(cache) {
                cache[key] = value
            }
        }
        return value
    }
}