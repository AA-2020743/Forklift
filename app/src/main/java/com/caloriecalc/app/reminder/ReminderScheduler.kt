package com.caloriecalc.app.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    fun scheduleWeightReminder(hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        val initialDelayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(0)

        val request = PeriodicWorkRequestBuilder<WeightReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Queues the nudge that fires when the first reminder went unanswered. The daily worker
     * only runs once per day, so the follow-up needs its own one-time job; it replaces any
     * pending one so a day's escalation can never stack on top of a previous day's.
     */
    fun scheduleWeightFollowUp(delayMinutes: Long) {
        val request = OneTimeWorkRequestBuilder<WeightReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(Data.Builder().putBoolean(WeightReminderWorker.KEY_IS_FOLLOW_UP, true).build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            FOLLOW_UP_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelWeightReminder() {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(WORK_NAME)
            cancelUniqueWork(FOLLOW_UP_WORK_NAME)
        }
    }

    companion object {
        private const val WORK_NAME = "weight_reminder_work"
        private const val FOLLOW_UP_WORK_NAME = "weight_reminder_follow_up_work"

        /** How long after the configured reminder time to nudge again if nothing was logged. */
        const val FOLLOW_UP_DELAY_MINUTES = 210L
    }
}
