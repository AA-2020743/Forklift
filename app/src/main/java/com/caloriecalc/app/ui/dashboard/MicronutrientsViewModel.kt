package com.caloriecalc.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.repository.MealSlotRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.domain.MicronutrientEstimator
import com.caloriecalc.app.domain.MicronutrientReference
import com.caloriecalc.app.domain.MicronutrientTarget
import com.caloriecalc.app.domain.NutritionCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MicronutrientRow(
    val target: MicronutrientTarget,
    val consumed: Double
)

data class MicronutrientsUiState(
    val rows: List<MicronutrientRow> = emptyList(),
    /** How many of the selected day's logged foods actually carry micronutrient data. */
    val foodsWithData: Int = 0,
    val foodsLogged: Int = 0,
    /** Names of logged foods contributing nothing, so the gap is nameable rather than mysterious. */
    val foodsMissingData: List<String> = emptyList(),
    val sugarContributions: List<MacroContribution> = emptyList(),
    val saturatedFatContributions: List<MacroContribution> = emptyList()
)

class MicronutrientsViewModel(
    private val epochDay: Long,
    profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository,
    mealSlotRepository: MealSlotRepository
) : ViewModel() {

    val uiState: StateFlow<MicronutrientsUiState> = combine(
        profileRepository.observeProfile(),
        nutritionLogRepository.totalsForDay(epochDay),
        nutritionLogRepository.entriesForDay(epochDay),
        mealSlotRepository.observeAll()
    ) { profile, totals, entries, mealSlots ->
        val consumedByLabel = mapOf(
            "Fiber" to totals.fiberGrams,
            "Sugar" to totals.sugarGrams,
            "Saturated fat" to totals.saturatedFatGrams,
            "Sodium" to totals.sodiumMg,
            "Potassium" to totals.potassiumMg,
            "Calcium" to totals.calciumMg,
            "Iron" to totals.ironMg,
            "Vitamin C" to totals.vitaminCMg,
            "Vitamin D" to totals.vitaminDMcg,
            "Vitamin B12" to totals.vitaminB12Mcg,
            "Magnesium" to totals.magnesiumMg,
            "Zinc" to totals.zincMg
        )
        val calorieTarget = NutritionCalculator.computeTargets(profile).calorieTarget
        val rows = MicronutrientReference.targets(profile.sex, calorieTarget).map { target ->
            MicronutrientRow(target, consumedByLabel[target.label] ?: 0.0)
        }
        val mealNameById = mealSlots.associate { it.id to it.name }
        val missing = entries
            .filter { MicronutrientEstimator.isEmpty(it.entry.micronutrients) }
            .map { it.food.name }
            .distinct()
        MicronutrientsUiState(
            rows = rows,
            foodsWithData = entries.size - entries.count { MicronutrientEstimator.isEmpty(it.entry.micronutrients) },
            foodsLogged = entries.size,
            foodsMissingData = missing,
            sugarContributions = macroContributions(
                entries,
                mealNameById,
                macroOf = { it.micronutrients.sugarGrams ?: 0.0 }
            ),
            saturatedFatContributions = macroContributions(
                entries,
                mealNameById,
                macroOf = { it.micronutrients.saturatedFatGrams ?: 0.0 }
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MicronutrientsUiState())
}
