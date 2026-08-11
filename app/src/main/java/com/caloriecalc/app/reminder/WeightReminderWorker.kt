package com.caloriecalc.app.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caloriecalc.app.CalorieCalcApp
import java.time.LocalDate

class WeightReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CalorieCalcApp).appContainer
        val profile = container.profileRepository.getProfile()
        if (!profile.weightReminderEnabled) return Result.success()

        val today = LocalDate.now().toEpochDay()
        val alreadyLogged = container.weightRepository.hasLoggedForDay(today)
        if (!alreadyLogged) {
            NotificationHelper.showWeightReminder(applicationContext)
        }
        return Result.success()
    }
}
