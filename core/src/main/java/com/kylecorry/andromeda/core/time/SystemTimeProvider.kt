package com.kylecorry.andromeda.core.time

import android.os.SystemClock

class SystemTimeProvider : TimeProvider {
    override fun elapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}
