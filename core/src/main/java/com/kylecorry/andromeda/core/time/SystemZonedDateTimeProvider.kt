package com.kylecorry.andromeda.core.time

import java.time.ZonedDateTime

class SystemZonedDateTimeProvider : IZonedDateTimeProvider {
    override fun getTime(): ZonedDateTime {
        return ZonedDateTime.now()
    }
}
