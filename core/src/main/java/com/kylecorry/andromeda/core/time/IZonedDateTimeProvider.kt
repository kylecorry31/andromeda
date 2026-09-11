package com.kylecorry.andromeda.core.time

import java.time.ZonedDateTime

interface IZonedDateTimeProvider {
    fun getTime(): ZonedDateTime
}
