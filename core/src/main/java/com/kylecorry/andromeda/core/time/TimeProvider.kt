package com.kylecorry.andromeda.core.time

interface TimeProvider {
    fun elapsedRealtime(): Long
    fun currentTimeMillis(): Long
}
