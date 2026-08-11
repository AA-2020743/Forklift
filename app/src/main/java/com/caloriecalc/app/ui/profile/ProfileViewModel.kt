package com.caloriecalc.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.UserProfile
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.domain.NutritionCalculator
import com.caloriecalc.app.domain.NutritionTargets
import com.caloriecalc.app.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val targets: NutritionTargets = NutritionCalculator.computeTargets(UserProfile())
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = profileRepository.observeProfile()
        .map { ProfileUiState(profile = it, targets = NutritionCalculator.computeTargets(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileRepository.updateProfile(profile)
            if (profile.weightReminderEnabled) {
                reminderScheduler.scheduleWeightReminder(profile.weightReminderHour, profile.weightReminderMinute)
            } else {
                reminderScheduler.cancelWeightReminder()
            }
        }
    }
}
