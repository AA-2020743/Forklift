package com.caloriecalc.app.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caloriecalc.app.CalorieCalcApp
import java.time.LocalDate

/**
 * Runs twice for a given day: once at the user's configured reminder time, and — only if that
 * first nudge went unanswered — again a few hours later via a one-time follow-up this worker
 * queues for itself. The main (non-follow-up) firing also re-queues itself for the next day's
 * reminder time before returning — see ReminderScheduler.scheduleWeightReminder for why that's
 * a self-chained one-time job rather than a PeriodicWorkRequest.
 */
class WeightReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CalorieCalcApp).appContainer
        val profile = container.profileRepository.getProfile()
        if (!profile.weightReminderEnabled) return Result.success()

        val isFollowUp = inputData.getBoolean(KEY_IS_FOLLOW_UP, false)
        val today = LocalDate.now().toEpochDay()
        val alreadyLogged = container.weightRepository.hasLoggedForDay(today)

        if (!alreadyLogged) {
            if (isFollowUp) {
                NotificationHelper.showWeightFollowUpReminder(applicationContext)
            } else {
                NotificationHelper.showWeightReminder(applicationContext)
                container.reminderScheduler.scheduleWeightFollowUp(ReminderScheduler.FOLLOW_UP_DELAY_MINUTES)
            }
        }

        // Only the main daily firing owns the chain to tomorrow — the follow-up is a one-off
        // for today and must not itself schedule another day's cycle.
        if (!isFollowUp) {
            container.reminderScheduler.scheduleWeightReminder(profile.weightReminderHour, profile.weightReminderMinute)
        }

        return Result.success()
    }

    companion object {
        const val KEY_IS_FOLLOW_UP = "is_follow_up"
    }
}
