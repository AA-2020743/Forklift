package com.caloriecalc.app.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    /**
     * Schedules the next firing of the daily weight reminder as a one-time job, not a
     * PeriodicWorkRequest. A `PeriodicWorkRequestBuilder(1, TimeUnit.DAYS)` with no flex
     * interval treats its *entire* 24h period as eligible to run in, not just the moment the
     * initial delay lands on — so after the first correctly-timed fire, Android is free to run
     * every later occurrence any time that day it finds convenient for batching, which in
     * practice drifts the reminder earlier and earlier. WeightReminderWorker re-calls this
     * itself after each firing to queue the next day's exact time, so every occurrence gets a
     * freshly computed initial delay instead of drifting inside an unconstrained window.
     */
    fun scheduleWeightReminder(hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        val initialDelayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(0)

        val request = OneTimeWorkRequestBuilder<WeightReminderWorker>()
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
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

    /**
     * Queues the next protein-spacing check. Like the weight reminder this is a self-chaining
     * one-time job rather than periodic work, so ProteinGapWorker can vary the next delay —
     * normally hourly, but jumping straight to wake-up time when it runs during sleep.
     */
    fun scheduleProteinGapCheck(delayMinutes: Long) {
        val request = OneTimeWorkRequestBuilder<ProteinGapWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            PROTEIN_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelProteinGapCheck() {
        WorkManager.getInstance(context).cancelUniqueWork(PROTEIN_WORK_NAME)
    }

    companion object {
        private const val WORK_NAME = "weight_reminder_work"
        private const val FOLLOW_UP_WORK_NAME = "weight_reminder_follow_up_work"
        private const val PROTEIN_WORK_NAME = "protein_gap_work"

        /** How long after the configured reminder time to nudge again if nothing was logged. */
        const val FOLLOW_UP_DELAY_MINUTES = 210L

        /** How often to re-evaluate the protein gap while awake. */
        const val PROTEIN_CHECK_INTERVAL_MINUTES = 60L
    }
}
