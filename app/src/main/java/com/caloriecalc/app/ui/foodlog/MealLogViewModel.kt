package com.caloriecalc.app.ui.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.repository.MealEntryWithFood
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.domain.MealType
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealLogViewModel(
    private val mealType: MealType,
    private val nutritionLogRepository: NutritionLogRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    val entries: StateFlow<List<MealEntryWithFood>> =
        nutritionLogRepository.entriesForMeal(today, mealType)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteEntry(entryWithFood: MealEntryWithFood) {
        viewModelScope.launch {
            nutritionLogRepository.deleteEntry(entryWithFood.entry)
        }
    }
}
