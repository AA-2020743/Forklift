package com.caloriecalc.app

import android.app.Application
import com.caloriecalc.app.di.AppContainer
import com.caloriecalc.app.reminder.NotificationHelper
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
            val profile = appContainer.profileRepository.getProfile()
            if (profile.weightReminderEnabled) {
                appContainer.reminderScheduler.scheduleWeightReminder(
                    profile.weightReminderHour,
                    profile.weightReminderMinute
                )
            }
        }
    }
}
