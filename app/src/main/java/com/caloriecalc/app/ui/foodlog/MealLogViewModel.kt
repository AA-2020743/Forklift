package com.caloriecalc.app.ui.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.repository.MealEntryWithFood
import com.caloriecalc.app.data.repository.MealSlotRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MealLogViewModel(
    private val mealSlotId: Long,
    val epochDay: Long,
    private val nutritionLogRepository: NutritionLogRepository,
    private val mealSlotRepository: MealSlotRepository
) : ViewModel() {

    val isToday: Boolean = epochDay == LocalDate.now().toEpochDay()

    private val _mealSlot = MutableStateFlow<MealSlot?>(null)
    val mealSlot: StateFlow<MealSlot?> = _mealSlot.asStateFlow()

    val entries: StateFlow<List<MealEntryWithFood>> =
        nutritionLogRepository.entriesForMeal(epochDay, mealSlotId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _mealSlot.value = mealSlotRepository.getById(mealSlotId)
        }
    }

    fun deleteEntry(entryWithFood: MealEntryWithFood) {
        viewModelScope.launch {
            nutritionLogRepository.deleteEntry(entryWithFood.entry)
        }
    }

    fun updateEntryQuantity(entryWithFood: MealEntryWithFood, grams: Double) {
        viewModelScope.launch {
            nutritionLogRepository.updateEntryQuantity(entryWithFood.entry, entryWithFood.food, grams)
        }
    }
}
