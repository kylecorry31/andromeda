package com.kylecorry.andromeda.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.kylecorry.andromeda.core.toDoubleCompat
import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.andromeda.core.tryOrDefault
import com.kylecorry.sol.units.Coordinate
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

class DefaultSharedPreferences(context: Context, private val commitChanges: Boolean = false) :
    IPreferences {

    /**
     * Same as mode private shared prefs with name <<package>>_preferences
     */
    private val sharedPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(context.applicationContext) }

    override val onChange = Topic.lazy<String>(
        { sharedPrefs.registerOnSharedPreferenceChangeListener(listener) },
        { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    )

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            key?.let { onChange.publish(it) }
        }

    override fun remove(key: String) {
        sharedPrefs?.edit(commitChanges) { remove(key) }
    }

    override fun contains(key: String): Boolean {
        return sharedPrefs?.contains(key) ?: false
    }

    override fun putInt(key: String, value: Int) {
        sharedPrefs?.edit(commitChanges) { putInt(key, value) }
    }

    override fun putBoolean(key: String, value: Boolean) {
        sharedPrefs?.edit(commitChanges) { putBoolean(key, value) }
    }

    override fun putString(key: String, value: String) {
        sharedPrefs?.edit(commitChanges) { putString(key, value) }
    }

    override fun putFloat(key: String, value: Float) {
        sharedPrefs?.edit(commitChanges) { putFloat(key, value) }
    }

    override fun putDouble(key: String, value: Double) {
        sharedPrefs?.edit(commitChanges) { putString(key, value.toString()) }
    }

    override fun putLong(key: String, value: Long) {
        sharedPrefs?.edit(commitChanges) { putLong(key, value) }
    }

    override fun getInt(key: String): Int? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getInt(key, 0)
    }

    override fun getBoolean(key: String): Boolean? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getBoolean(key, false)
    }

    override fun getString(key: String): String? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getString(key, null)
    }

    override fun getFloat(key: String): Float? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getFloat(key, 0f)
    }

    override fun getDouble(key: String): Double? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getString(key, null)?.toDoubleCompat()
    }

    override fun getLong(key: String): Long? {
        if (!contains(key)) {
            return null
        }
        return sharedPrefs?.getLong(key, 0L)
    }

    override fun putCoordinate(key: String, value: Coordinate) {
        putString(key, "${value.latitude},${value.longitude}")
    }

    override fun getCoordinate(key: String): Coordinate? {
        val raw = getString(key) ?: return null
        val parts = raw.split(",")
        if (parts.size != 2) {
            return null
        }
        return tryOrDefault(null) {
            Coordinate(parts[0].toDouble(), parts[1].toDouble())
        }
    }

    override fun getLocalDate(key: String): LocalDate? {
        val raw = getString(key) ?: return null
        return try {
            LocalDate.parse(raw)
        } catch (e: Exception) {
            null
        }
    }

    override fun putLocalDate(key: String, date: LocalDate) {
        putString(key, date.toString())
    }

    override fun putInstant(key: String, value: Instant) {
        putLong(key, value.toEpochMilli())
    }

    override fun getInstant(key: String): Instant? {
        val time = getLong(key) ?: return null
        return Instant.ofEpochMilli(time)
    }

    override fun putDuration(key: String, duration: Duration) {
        putLong(key, duration.toMillis())
    }

    override fun getAll(): Collection<Preference> {
        val all = sharedPrefs?.all ?: emptyMap<String, Any>()
        return all.mapNotNull { (key, value) ->
            val type = getPreferenceType(value)
            type?.let { Preference(key, it, value) }
        }
    }

    override fun putAll(preferences: Collection<Preference>, clearOthers: Boolean) {
        sharedPrefs.edit(commitChanges) {
            if (clearOthers) {
                clear()
            }
            preferences.forEach {
                when (it.type) {
                    PreferenceType.Int -> putInt(it.key, it.value as Int)
                    PreferenceType.Boolean -> putBoolean(it.key, it.value as Boolean)
                    PreferenceType.String -> putString(it.key, it.value as String)
                    PreferenceType.Float -> putFloat(it.key, it.value as Float)
                    PreferenceType.Long -> putLong(it.key, it.value as Long)
                }
            }
        }
    }

    override fun clear() {
        sharedPrefs.edit(commitChanges) { clear() }
    }

    override fun close() {
        sharedPrefs?.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun getDuration(key: String): Duration? {
        val millis = getLong(key) ?: return null
        return Duration.ofMillis(millis)
    }
}