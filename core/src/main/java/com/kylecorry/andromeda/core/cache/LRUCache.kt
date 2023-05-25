package com.kylecorry.andromeda.core.cache

import com.kylecorry.sol.time.Time.isInPast
import com.kylecorry.sol.time.Time.isOlderThan
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant

/**
 * A coroutine safe LRU cache, designed for use with IO operations
 */
class LRUCache<K, T>(private val size: Int? = null, private val duration: Duration? = null) {

    private var values: MutableMap<K, T> = mutableMapOf()
    private var cachedAt: MutableMap<K, Instant> = mutableMapOf()
    private var mutex = Mutex()

    suspend fun get(key: K): T? {
        return mutex.withLock {
            if (hasValidCache(key)) {
                values[key]
            } else {
                null
            }
        }
    }

    suspend fun put(key: K, value: T) {
        mutex.withLock {
            values[key] = value
            cachedAt[key] = Instant.now()
            removeOldest()
        }
    }

    suspend fun getOrPut(key: K, lookup: suspend () -> T): T {
        return mutex.withLock {
            if (hasValidCache(key)) {
                @Suppress("UNCHECKED_CAST")
                return@withLock values[key] as T
            }
            val newValue = lookup()
            values[key] = newValue
            cachedAt[key] = Instant.now()
            removeOldest()
            newValue
        }
    }

    suspend fun invalidate(key: K) {
        mutex.withLock {
            values.remove(key)
            cachedAt.remove(key)
        }
    }

    private fun removeOldest(){
        if (size == null){
            return
        }
        if (values.size <= size){
            return
        }
        val oldest = cachedAt.minByOrNull { it.value }?.key ?: return
        values.remove(oldest)
        cachedAt.remove(oldest)
    }

    private fun hasValidCache(key: K): Boolean {
        return values.containsKey(key) && !isCacheExpired(key)
    }

    private fun isCacheExpired(key: K): Boolean {
        if (duration == null) {
            return false
        }

        val time = cachedAt[key] ?: return false

        return !time.isInPast() || time.isOlderThan(duration)
    }

}