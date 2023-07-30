package com.kylecorry.andromeda.background

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.kylecorry.andromeda.core.system.Wakelocks
import com.kylecorry.andromeda.core.tryOrDefault
import com.kylecorry.andromeda.preferences.IPreferences
import com.kylecorry.andromeda.preferences.SharedPreferences
import com.kylecorry.sol.time.Time.toZonedDateTime
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

abstract class DailyWorker(
    context: Context,
    params: WorkerParameters,
    private val tolerance: Duration = Duration.ofMinutes(30),
    private val wakelockDuration: Duration? = null,
    private val getPreferences: (Context) -> IPreferences = { SharedPreferences(it) }
) : CoroutineWorker(context, params) {

    private val lock = Object()

    override suspend fun doWork(): Result {

        val wakelock = if (wakelockDuration != null) {
            val wakelockTag = "$uniqueId::interval::wakelock"
            tryOrDefault(null) {
                Wakelocks.get(applicationContext, wakelockTag)
                    ?.also { it.acquire(wakelockDuration.toMillis()) }
            }
        } else {
            null
        }

        try {
            val context = applicationContext

            val jobState = synchronized(lock) {
                val now = LocalDateTime.now()
                val cache = getPreferences(context)
                val lastRun = cache.getLocalDate(getLastRunKey(context))
                val shouldSend = isEnabled(context) && lastRun != now.toLocalDate()

                val sendTime = LocalDate.now().atTime(getScheduledTime(context))
                val tomorrowSendTime =
                    LocalDate.now().plusDays(1).atTime(getScheduledTime(context))

                val sendWindowStart = sendTime - tolerance
                val sendWindowEnd = sendTime + tolerance

                val inWindow = now.isAfter(sendWindowStart) && now.isBefore(sendWindowEnd)
                val isTooEarly = now.isBefore(sendWindowStart)
                val isAfterWindow = now.isAfter(sendWindowEnd)

                if (inWindow && shouldSend) {
                    Log.d(
                        javaClass.simpleName,
                        "Received a broadcast and executed"
                    )
                    cache.putLocalDate(getLastRunKey(context), now.toLocalDate())
                    return@synchronized true to tomorrowSendTime
                }

                if (isTooEarly) {
                    Log.d(
                        javaClass.simpleName,
                        "Received a broadcast too early"
                    )
                    return@synchronized false to sendTime
                }

                if (isAfterWindow || (inWindow && !shouldSend)) {
                    Log.d(
                        javaClass.simpleName,
                        "Received a broadcast too late, it already ran today, or it is not enabled"
                    )
                }

                return@synchronized false to tomorrowSendTime
            }

            try {
                if (jobState.first) {
                    val foregroundInfo = getForegroundInfo(context)
                    if (foregroundInfo != null) {
                        setForeground(foregroundInfo)
                    }
                    execute(context)
                }
            } finally {
                setAlarm(context, jobState.second)
            }
        } finally {
            wakelock?.release()
        }

        return Result.success()
    }

    protected open fun isEnabled(context: Context): Boolean {
        return true
    }

    protected abstract fun getScheduledTime(context: Context): LocalTime

    protected open fun getLastRunKey(context: Context): String {
        return "pref_andromeda_daily_worker_last_run_date_$uniqueId"
    }

    protected abstract suspend fun execute(context: Context)
    protected abstract val uniqueId: Int

    protected open fun getForegroundInfo(context: Context): ForegroundInfo? {
        return null
    }

    protected open fun getScheduler(context: Context): IOneTimeTaskScheduler {
        return OneTimeTaskSchedulerFactory(context).deferrable(this::class.java, uniqueId)
    }

    private fun setAlarm(context: Context, time: LocalDateTime) {
        val scheduler = getScheduler(context)
        scheduler.cancel()
        scheduler.once(time.toZonedDateTime().toInstant())
        Log.d(
            javaClass.simpleName,
            "Scheduled the next run for $time"
        )
    }
}