package com.caloriecalc.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.repository.MealSlotRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.domain.MacroEvaluator
import com.caloriecalc.app.domain.MacroProgress
import com.caloriecalc.app.domain.MacroStatus
import com.caloriecalc.app.domain.NutritionCalculator
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MealSummary(val mealSlot: MealSlot, val calories: Int, val itemCount: Int)

private val emptyMacroProgress = MacroProgress(0.0, 0.0, 0.0, MacroStatus.IN_RANGE)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val calorieConsumed: Int = 0,
    val calorieTarget: Int = 0,
    val proteinProgress: MacroProgress = emptyMacroProgress,
    val fatProgress: MacroProgress = emptyMacroProgress,
    val carbProgress: MacroProgress = emptyMacroProgress,
    val mealSummaries: List<MealSummary> = emptyList()
)

class DashboardViewModel(
    profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository,
    mealSlotRepository: MealSlotRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.observeProfile(),
        nutritionLogRepository.totalsForDay(today),
        nutritionLogRepository.entriesForDay(today),
        mealSlotRepository.observeActive()
    ) { profile, totals, entries, mealSlots ->
        val targets = NutritionCalculator.computeTargets(profile)
        val mealSummaries = mealSlots.map { slot ->
            val mealEntries = entries.filter { it.entry.mealSlotId == slot.id }
            MealSummary(
                mealSlot = slot,
                calories = mealEntries.sumOf { it.entry.calories }.roundToInt(),
                itemCount = mealEntries.size
            )
        }
        DashboardUiState(
            isLoading = false,
            calorieConsumed = totals.calories.roundToInt(),
            calorieTarget = targets.calorieTarget,
            proteinProgress = MacroEvaluator.evaluate(totals.protein, targets.protein),
            fatProgress = MacroEvaluator.evaluate(totals.fat, targets.fat),
            carbProgress = MacroEvaluator.evaluate(totals.carbs, targets.carbs),
            mealSummaries = mealSummaries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
