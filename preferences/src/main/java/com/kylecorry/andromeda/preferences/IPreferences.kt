package com.kylecorry.andromeda.preferences

import com.kylecorry.andromeda.core.topics.generic.Topic
import com.kylecorry.sol.units.Coordinate
import java.io.Closeable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

interface IPreferences: Closeable {
    val onChange: Topic<String>
    fun remove(key: String)
    fun contains(key: String): Boolean
    fun putInt(key: String, value: Int)
    fun putBoolean(key: String, value: Boolean)
    fun putString(key: String, value: String)
    fun putFloat(key: String, value: Float)
    fun putDouble(key: String, value: Double)
    fun putLong(key: String, value: Long)
    fun getInt(key: String): Int?
    fun getBoolean(key: String): Boolean?
    fun getString(key: String): String?
    fun getFloat(key: String): Float?
    fun getDouble(key: String): Double?
    fun getLong(key: String): Long?
    fun putCoordinate(key: String, value: Coordinate)
    fun getCoordinate(key: String): Coordinate?
    fun getLocalDate(key: String): LocalDate?
    fun putLocalDate(key: String, date: LocalDate)
    fun putInstant(key: String, value: Instant)
    fun getInstant(key: String): Instant?
    fun getDuration(key: String): Duration?
    fun putDuration(key: String, duration: Duration)
    fun getAll(): Map<String, *>
}