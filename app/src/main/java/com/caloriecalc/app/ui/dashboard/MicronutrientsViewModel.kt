package com.caloriecalc.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val consumed: Double,
    val hasData: Boolean = true
)

data class MicronutrientsUiState(
    val rows: List<MicronutrientRow> = emptyList(),
    /** How many of the selected day's logged foods actually carry micronutrient data. */
    val foodsWithData: Int = 0,
    val foodsLogged: Int = 0,
    /** Names of logged foods contributing nothing, so the gap is nameable rather than mysterious. */
    val foodsMissingData: List<String> = emptyList()
)

class MicronutrientsViewModel(
    private val epochDay: Long,
    profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository
) : ViewModel() {

    val uiState: StateFlow<MicronutrientsUiState> = combine(
        profileRepository.observeProfile(),
        nutritionLogRepository.totalsForDay(epochDay),
        nutritionLogRepository.entriesForDay(epochDay)
    ) { profile, totals, entries ->
        val addedSugarValues = entries.mapNotNull { it.entry.micronutrients.addedSugarGrams }
        val consumedByLabel = mapOf(
            "Fiber" to totals.fiberGrams,
            "Total sugar" to totals.sugarGrams,
            "Added sugar" to addedSugarValues.sum(),
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
            MicronutrientRow(
                target = target,
                consumed = consumedByLabel[target.label] ?: 0.0,
                hasData = target.label != "Added sugar" || addedSugarValues.isNotEmpty()
            )
        }
        val missing = entries
            .filter { MicronutrientEstimator.isEmpty(it.entry.micronutrients) }
            .map { it.food.name }
            .distinct()
        MicronutrientsUiState(
            rows = rows,
            foodsWithData = entries.size - entries.count { MicronutrientEstimator.isEmpty(it.entry.micronutrients) },
            foodsLogged = entries.size,
            foodsMissingData = missing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MicronutrientsUiState())
}
