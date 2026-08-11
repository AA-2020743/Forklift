package com.caloriecalc.app.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.WeightLog
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.data.repository.WeightRepository
import com.caloriecalc.app.domain.NutritionCalculator
import com.caloriecalc.app.domain.WeightTrendAnalyzer
import com.caloriecalc.app.domain.WeightTrendResult
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val WINDOW_DAYS = 28L

data class WeightUiState(
    val latestWeightKg: Double? = null,
    val weightLogs: List<WeightLog> = emptyList(),
    val dailyCalories: Map<Long, Double> = emptyMap(),
    val trend: WeightTrendResult? = null
)

class WeightViewModel(
    private val weightRepository: WeightRepository,
    private val profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()
    private val windowStart = today - (WINDOW_DAYS - 1)

    val uiState: StateFlow<WeightUiState> = combine(
        weightRepository.observeInRange(windowStart, today),
        nutritionLogRepository.totalsInRange(windowStart, today),
        profileRepository.observeProfile()
    ) { logs, totals, profile ->
        val caloriesByDay = totals.associate { it.epochDay to it.calories }
        val targets = NutritionCalculator.computeTargets(profile)
        val trend = WeightTrendAnalyzer.analyze(
            weightLogsInWindow = logs,
            dailyCaloriesByEpochDay = caloriesByDay,
            calorieTarget = targets.calorieTarget,
            goal = profile.goal,
            currentBodyWeightKg = profile.bodyWeightKg
        )
        WeightUiState(
            latestWeightKg = logs.maxByOrNull { it.epochDay }?.weightKg,
            weightLogs = logs,
            dailyCalories = caloriesByDay,
            trend = trend
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUiState())

    fun logWeight(weightKg: Double) {
        viewModelScope.launch {
            weightRepository.logWeight(today, weightKg)
            val profile = profileRepository.getProfile()
            profileRepository.updateProfile(profile.copy(bodyWeightKg = weightKg))
        }
    }
}
