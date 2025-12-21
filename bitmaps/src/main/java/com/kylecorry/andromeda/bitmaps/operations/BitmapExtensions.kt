package com.kylecorry.andromeda.bitmaps.operations

import android.graphics.Bitmap
import com.kylecorry.luna.coroutines.Parallel

fun Bitmap.applyOperations(
    vararg operations: BitmapOperation,
    recycleOriginal: Boolean = true,
    forceGarbageCollection: Boolean = false
): Bitmap {
    return applyOperations(
        operations.toList(),
        recycleOriginal,
        forceGarbageCollection
    )
}

fun Bitmap.applyOperationsOrNull(
    vararg operations: BitmapOperation,
    recycleOriginal: Boolean = true,
    recycleOriginalOnError: Boolean = true,
    forceGarbageCollection: Boolean = false,
): Bitmap? {
    return try {
        applyOperations(operations.toList(), recycleOriginal, forceGarbageCollection)
    } catch (e: Exception) {
        e.printStackTrace()
        if (recycleOriginalOnError && recycleOriginal) {
            recycle()
        }
        null
    }
}

fun Bitmap.applyOperations(
    operations: List<BitmapOperation>,
    recycleOriginal: Boolean = true,
    forceGarbageCollection: Boolean = false
): Bitmap {
    var current = this
    var last = this
    operations.forEach {
        current = it.execute(current)
        if ((recycleOriginal || last != this) && current != last) {
            last.recycle()
        }
        last = current
    }

    if (forceGarbageCollection) {
        System.gc()
    }
    return current
}

fun Bitmap.getPixels(): IntArray {
    val pixels = IntArray(this.width * this.height)
    this.getPixels(pixels, 0, this.width, 0, 0, this.width, this.height)
    return pixels
}

fun Bitmap.setPixels(pixels: IntArray) {
    this.setPixels(pixels, 0, this.width, 0, 0, this.width, this.height)
}

suspend inline fun parallelForEachIndex(
    size: Int,
    maxParallel: Int = Runtime.getRuntime().availableProcessors(),
    crossinline action: (Int) -> Unit
) {
    val chunkSize = size / maxParallel + 1

    val coroutines = mutableListOf<suspend () -> Unit>()
    for (t in 0 until maxParallel) {
        val start = t * chunkSize
        val end = minOf(start + chunkSize, size)
        if (start >= end) {
            continue
        }
        coroutines.add {
            for (i in start until end) {
                action(i)
            }
        }
    }

    Parallel.forEach(coroutines, maxParallel)
}

suspend inline fun <reified T> parallelReduceIndex(
    size: Int,
    initialValue: T,
    maxParallel: Int = Runtime.getRuntime().availableProcessors(),
    crossinline reducer: (T, Int) -> T,
    crossinline combiner: (T, T) -> T
): T {
    val chunkSize = size / maxParallel + 1

    val coroutines = mutableListOf<suspend () -> Unit>()
    val results = Array<T?>(maxParallel) { null }
    for (t in 0 until maxParallel) {
        val start = t * chunkSize
        val end = minOf(start + chunkSize, size)
        if (start >= end) {
            continue
        }
        coroutines.add {
            var acc = initialValue
            for (i in start until end) {
                acc = reducer(acc, i)
            }
            results[t] = acc
        }
    }

    Parallel.forEach(coroutines, maxParallel)
    var finalAcc = initialValue
    for (res in results) {
        if (res != null) {
            finalAcc = combiner(finalAcc, res)
        }
    }
    return finalAcc
}

suspend inline fun <reified T> Bitmap.reducePixels(
    initialValue: T,
    crossinline operation: (acc: T, pixel: Int) -> T,
    crossinline combiner: (a: T, b: T) -> T
): T {
    val pixels = getPixels()
    return parallelReduceIndex(pixels.size, initialValue, reducer = { acc, i ->
        operation(acc, pixels[i])
    }, combiner = combiner)
}

inline fun IntArray.get(x: Int, y: Int, width: Int): Int {
    return this[y * width + x]
}

inline fun IntArray.set(x: Int, y: Int, width: Int, value: Int) {
    this[y * width + x] = value
}