package com.kylecorry.andromeda.core.time

import java.time.ZonedDateTime

class SystemTimeProvider : ITimeProvider {
    override fun getTime(): ZonedDateTime {
        return ZonedDateTime.now()
    }
}