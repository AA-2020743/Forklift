package com.caloriecalc.app

import android.app.Application
import com.caloriecalc.app.di.AppContainer
import com.caloriecalc.app.reminder.NotificationHelper
import com.caloriecalc.app.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalorieCalcApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        NotificationHelper.createChannels(this)

        applicationScope.launch {
            appContainer.workoutRepository.ensureSeeded()
            appContainer.mealSlotRepository.ensureSeeded()
            // Repairs foods saved before micronutrient auto-fill existed; a no-op once done,
            // since it only touches foods that still have nothing recorded.
            appContainer.micronutrientBackfill.run()
            val profile = appContainer.profileRepository.getProfile()
            if (profile.weightReminderEnabled) {
                appContainer.reminderScheduler.scheduleWeightReminder(
                    profile.weightReminderHour,
                    profile.weightReminderMinute
                )
            }
            if (profile.proteinReminderEnabled) {
                appContainer.reminderScheduler.scheduleProteinGapCheck(
                    ReminderScheduler.PROTEIN_CHECK_INTERVAL_MINUTES
                )
            }
        }
    }
}
