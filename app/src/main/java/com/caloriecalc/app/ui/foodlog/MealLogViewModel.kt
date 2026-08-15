package com.caloriecalc.app.ui.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.repository.MealEntryWithFood
import com.caloriecalc.app.data.repository.MealSlotRepository
import com.caloriecalc.app.data.repository.MealTemplateRepository
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
    private val mealSlotRepository: MealSlotRepository,
    private val mealTemplateRepository: MealTemplateRepository
) : ViewModel() {

    val isToday: Boolean = epochDay == LocalDate.now().toEpochDay()

    private val _mealSlot = MutableStateFlow<MealSlot?>(null)
    val mealSlot: StateFlow<MealSlot?> = _mealSlot.asStateFlow()

    val entries: StateFlow<List<MealEntryWithFood>> =
        nutritionLogRepository.entriesForMeal(epochDay, mealSlotId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Only meaningful when [isToday] — powers the "Repeat yesterday" quick action. Harmless
     * (just an extra query) to compute when viewing a past day, where it's simply unused. */
    val yesterdayEntries: StateFlow<List<MealEntryWithFood>> =
        nutritionLogRepository.entriesForMeal(epochDay - 1, mealSlotId)
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

    fun repeatYesterday() {
        if (!isToday) return
        viewModelScope.launch {
            nutritionLogRepository.repeatMeal(epochDay - 1, epochDay, mealSlotId)
        }
    }

    fun saveAsTemplate(name: String) {
        viewModelScope.launch {
            mealTemplateRepository.saveFromMeal(name, epochDay, mealSlotId)
        }
    }
}
