package com.kylecorry.andromeda.preferences

import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.sol.units.Coordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

class CachedPreferences(
    private val preferences: IPreferences,
    private val shouldPreloadAllPrefs: Boolean = false
) : IPreferences {
    override val onChange: Topic<String> = preferences.onChange

    private val cache = ConcurrentHashMap<String, Any?>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val runner = ControlledRunner<Unit>()

    init {
        loadPrefs()
        preferences.onChange.subscribe(this::onPreferenceChanged)
    }

    override fun remove(key: String) {
        cache.remove(key)
        preferences.remove(key)
    }

    override fun contains(key: String): Boolean {
        return cache.containsKey(key) || preferences.contains(key)
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
    override fun getAll(): Collection<Preference> {
        return preferences.getAll()
    }

    override fun putAll(preferences: Collection<Preference>, clearOthers: Boolean) {
        if (!clearOthers) {
            cache.clear()
        }

        preferences.forEach { preference ->
            cache[preference.key] = preference.value
        }

        this.preferences.putAll(preferences, clearOthers)
    }

    override fun clear() {
        cache.clear()
        preferences.clear()
    }

    override fun close() {
        runner.cancel()
        scope.cancel()
        preferences.onChange.unsubscribe(this::onPreferenceChanged)
        preferences.close()
    }

    private fun <T> put(key: String, value: T, setter: (String, T) -> Unit) {
        cache[key] = value
        setter(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> get(key: String, getter: (String) -> T?): T? {
        cache.getOrDefault(key, null)?.let {
            return it as T
        }

        val value = getter(key)
        if (value != null) {
            cache[key] = value
        }
        return value
    }

    private fun onPreferenceChanged(key: String): Boolean {
        cache.remove(key)
        loadPrefs(100)
        return true
    }

    private fun loadPrefs(debounce: Long = 0) {
        if (!shouldPreloadAllPrefs) {
            return
        }
        scope.launch {
            runner.cancelPreviousThenRun {
                // This will give some delay if a cancellation occurs, so it won't reload the prefs too often
                if (debounce > 0) {
                    delay(debounce)
                }
                val all = preferences.getAll()
                cache.putAll(all.map { it.key to it.value })
            }
        }
    }
}