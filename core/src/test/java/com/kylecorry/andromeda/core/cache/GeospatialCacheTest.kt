package com.kylecorry.andromeda.core.cache

import com.kylecorry.sol.units.Coordinate
import com.kylecorry.sol.units.Distance
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class GeospatialCacheTest {

    @Test
    fun get() = runBlocking {
        val cache = GeospatialCache<Int>(Distance.meters(1000f))

        // Put some values in the cache
        cache.put(Coordinate(0.0, 0.0), 0)
        cache.put(Coordinate(1.0, 1.0), 1)
        cache.put(Coordinate(1.005, 1.0), 2)
        cache.put(Coordinate(1.0, 2.0), 3)

        // Get the values
        assertEquals(0, cache.get(Coordinate(0.0, 0.0)))
        assertEquals(2, cache.get(Coordinate(1.0, 1.0)))
        assertEquals(2, cache.get(Coordinate(1.005, 1.0)))
        assertEquals(3, cache.get(Coordinate(1.0, 2.0)))
        assertNull(cache.get(Coordinate(2.0, 2.0)))
    }

    @Test
    fun getOrPut() = runBlocking {
        val cache = GeospatialCache<Int>(Distance.meters(1000f))

        assertEquals(0, cache.getOrPut(Coordinate(0.0, 0.0)) { 0 })
        assertEquals(1, cache.getOrPut(Coordinate(1.0, 1.0)) { 1 })
        assertEquals(1, cache.getOrPut(Coordinate(1.005, 1.0)) { 2 })
        assertEquals(3, cache.getOrPut(Coordinate(1.0, 2.0)) { 3 })
    }

    @Test
    fun invalidateAll() = runBlocking {
        val cache = GeospatialCache<Int>(Distance.meters(1000f))

        // Put some values in the cache
        cache.put(Coordinate(0.0, 0.0), 0)
        cache.put(Coordinate(1.0, 1.0), 1)
        cache.put(Coordinate(1.005, 1.0), 2)
        cache.put(Coordinate(1.0, 2.0), 3)

        // Invalidate the cache
        cache.invalidateAll()

        // Get the values
        assertNull(cache.get(Coordinate(0.0, 0.0)))
        assertNull(cache.get(Coordinate(1.0, 1.0)))
        assertNull(cache.get(Coordinate(1.005, 1.0)))
        assertNull(cache.get(Coordinate(1.0, 2.0)))
    }

    @Test
    fun invalidate() = runBlocking {
        val cache = GeospatialCache<Int>(Distance.meters(1000f))

        // Put some values in the cache
        cache.put(Coordinate(0.0, 0.0), 0)
        cache.put(Coordinate(1.0, 1.0), 1)
        cache.put(Coordinate(1.005, 1.0), 2)
        cache.put(Coordinate(1.0, 2.0), 3)

        // Invalidate the cache
        cache.invalidate(Coordinate(1.0, 1.0))

        // Get the values
        assertEquals(0, cache.get(Coordinate(0.0, 0.0)))
        assertNull(cache.get(Coordinate(1.0, 1.0)))
        assertNull(cache.get(Coordinate(1.005, 1.0)))
        assertEquals(3, cache.get(Coordinate(1.0, 2.0)))
    }

    @Test
    fun getWithExpiration() = runBlocking {
        var time = Instant.ofEpochMilli(0)
        val cache = GeospatialCache<Int>(
            Distance.meters(1000f),
            cacheDuration = Duration.ofSeconds(1),
            timeProvider = { time })

        // Put some values in the cache
        cache.put(Coordinate(0.0, 0.0), 0)

        // Can get the value before it expires
        assertEquals(0, cache.get(Coordinate(0.0, 0.0)))

        // Enter a second value
        time += Duration.ofMillis(10)
        cache.put(Coordinate(1.0, 1.0), 1)

        // Can't get the value after it expires
        time += Duration.ofMillis(991)
        assertNull(cache.get(Coordinate(0.0, 0.0)))

        // But can get the other value
        assertEquals(1, cache.get(Coordinate(1.0, 1.0)))

        // Can't get the value after it expires
        time += Duration.ofMillis(10)
        assertNull(cache.get(Coordinate(1.0, 1.0)))
    }

    @Test
    fun removesTheLastRecentlyUsedWhenOutOfSpace() = runBlocking {
        var time = Instant.ofEpochMilli(0)
        val cache = GeospatialCache<Int>(
            Distance.meters(1000f),
            size = 3,
            removalStrategy = GeospatialCache.RemovalStrategy.LeastRecentlyUsed,
            timeProvider = { time }
        )

        // Put some values in the cache
        cache.put(Coordinate(0.0, 0.0), 0)
        time += Duration.ofMillis(1)
        cache.put(Coordinate(1.0, 0.0), 1)
        time += Duration.ofMillis(1)
        cache.put(Coordinate(2.0, 0.0), 2)

        // Access the first value before entering the fourth
        time += Duration.ofMillis(1)
        cache.get(Coordinate(0.0, 0.0))
        time += Duration.ofMillis(1)
        cache.put(Coordinate(3.0, 0.0), 3)

        // Get the values
        assertEquals(0, cache.get(Coordinate(0.0, 0.0)))
        assertNull(cache.get(Coordinate(1.0, 0.0)))
        assertEquals(2, cache.get(Coordinate(2.0, 0.0)))
        assertEquals(3, cache.get(Coordinate(3.0, 0.0)))
    }

    @Test
    fun removesTheFurthestWhenOutOfSpace() = runBlocking {
        val cache = GeospatialCache<Int>(
            Distance.meters(1000f),
            size = 3,
            removalStrategy = GeospatialCache.RemovalStrategy.Furthest
        )

        // Put some values in the cache
        cache.put(Coordinate(1.0, 0.0), 0)
        cache.put(Coordinate(0.0, 0.0), 1)
        cache.put(Coordinate(2.0, 0.0), 2)
        cache.put(Coordinate(3.0, 0.0), 3)

        // Get the values
        assertNull(cache.get(Coordinate(0.0, 0.0)))
        assertEquals(0, cache.get(Coordinate(1.0, 0.0)))
        assertEquals(2, cache.get(Coordinate(2.0, 0.0)))
        assertEquals(3, cache.get(Coordinate(3.0, 0.0)))
    }
}