package com.caloriecalc.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.domain.MacroEvaluator
import com.caloriecalc.app.domain.MacroProgress
import com.caloriecalc.app.domain.MacroStatus
import com.caloriecalc.app.domain.MealType
import com.caloriecalc.app.domain.NutritionCalculator
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MealSummary(val mealType: MealType, val calories: Int, val itemCount: Int)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val calorieConsumed: Int = 0,
    val calorieTarget: Int = 0,
    val proteinProgress: MacroProgress = MacroProgress(0.0, 0.0, status = MacroStatus.ON_TARGET),
    val fatProgress: MacroProgress = MacroProgress(0.0, 0.0, status = MacroStatus.ON_TARGET),
    val carbProgress: MacroProgress = MacroProgress(0.0, 0.0, status = MacroStatus.ON_TARGET),
    val mealSummaries: List<MealSummary> = emptyList()
)

class DashboardViewModel(
    profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.observeProfile(),
        nutritionLogRepository.totalsForDay(today),
        nutritionLogRepository.entriesForDay(today)
    ) { profile, totals, entries ->
        val targets = NutritionCalculator.computeTargets(profile)
        val mealSummaries = MealType.entriesInDayOrder.map { mealType ->
            val mealEntries = entries.filter { it.entry.mealType == mealType }
            MealSummary(
                mealType = mealType,
                calories = mealEntries.sumOf { it.entry.calories }.roundToInt(),
                itemCount = mealEntries.size
            )
        }
        DashboardUiState(
            isLoading = false,
            calorieConsumed = totals.calories.roundToInt(),
            calorieTarget = targets.calorieTarget,
            proteinProgress = MacroEvaluator.evaluateProtein(totals.protein, targets.proteinTargetGrams),
            fatProgress = MacroEvaluator.evaluateFat(
                totals.fat,
                targets.fatMinGrams,
                targets.fatTargetGrams,
                targets.fatMaxGrams
            ),
            carbProgress = MacroEvaluator.evaluateCarbs(totals.carbs, targets.carbTargetGrams),
            mealSummaries = mealSummaries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
