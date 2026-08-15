package com.caloriecalc.app.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caloriecalc.app.CalorieCalcApp
import java.time.LocalDate

/**
 * Runs twice for a given day: once at the user's configured reminder time (the daily periodic
 * job), and — only if that first nudge went unanswered — again a few hours later via a one-time
 * follow-up this worker queues for itself.
 */
class WeightReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CalorieCalcApp).appContainer
        val profile = container.profileRepository.getProfile()
        if (!profile.weightReminderEnabled) return Result.success()

        val today = LocalDate.now().toEpochDay()
        if (container.weightRepository.hasLoggedForDay(today)) return Result.success()

        val isFollowUp = inputData.getBoolean(KEY_IS_FOLLOW_UP, false)
        if (isFollowUp) {
            NotificationHelper.showWeightFollowUpReminder(applicationContext)
        } else {
            NotificationHelper.showWeightReminder(applicationContext)
            container.reminderScheduler.scheduleWeightFollowUp(ReminderScheduler.FOLLOW_UP_DELAY_MINUTES)
        }
        return Result.success()
    }

    companion object {
        const val KEY_IS_FOLLOW_UP = "is_follow_up"
    }
}
