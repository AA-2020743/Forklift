package com.caloriecalc.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.domain.MicronutrientReference
import com.caloriecalc.app.domain.MicronutrientTarget
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MicronutrientRow(val target: MicronutrientTarget, val consumed: Double)

data class MicronutrientsUiState(val rows: List<MicronutrientRow> = emptyList())

class MicronutrientsViewModel(
    profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    val uiState: StateFlow<MicronutrientsUiState> = combine(
        profileRepository.observeProfile(),
        nutritionLogRepository.totalsForDay(today)
    ) { profile, totals ->
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
        val rows = MicronutrientReference.targets(profile.sex).map { target ->
            MicronutrientRow(target, consumedByLabel[target.label] ?: 0.0)
        }
        MicronutrientsUiState(rows)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MicronutrientsUiState())
}
