package com.kylecorry.andromeda.core.time

import java.time.ZonedDateTime

interface ITimeProvider {
    fun getTime(): ZonedDateTime
}