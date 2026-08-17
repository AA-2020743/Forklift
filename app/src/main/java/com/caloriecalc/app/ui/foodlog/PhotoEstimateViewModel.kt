package com.caloriecalc.app.ui.foodlog

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.remote.EstimatedFoodItemDto
import com.caloriecalc.app.data.repository.FoodRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.PhotoEstimationRepository
import com.caloriecalc.app.data.repository.toFoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditableEstimate(
    val original: EstimatedFoodItemDto,
    val name: String = original.name,
    val gramsText: String = formatEstimateNumber(original.estimatedGrams),
    val caloriesPer100gText: String = formatEstimateNumber(original.caloriesPer100g),
    val proteinPer100gText: String = formatEstimateNumber(original.proteinPer100g),
    val fatPer100gText: String = formatEstimateNumber(original.fatPer100g),
    val carbsPer100gText: String = formatEstimateNumber(original.carbsPer100g)
) {
    val grams: Double get() = gramsText.toDoubleOrNull() ?: 0.0
    val caloriesPer100g: Double get() = caloriesPer100gText.toDoubleOrNull() ?: 0.0
    val proteinPer100g: Double get() = proteinPer100gText.toDoubleOrNull() ?: 0.0
    val fatPer100g: Double get() = fatPer100gText.toDoubleOrNull() ?: 0.0
    val carbsPer100g: Double get() = carbsPer100gText.toDoubleOrNull() ?: 0.0

    val isValid: Boolean
        get() = name.isNotBlank() && grams.isFinite() && grams > 0.0 &&
            listOf(caloriesPer100g, proteinPer100g, fatPer100g, carbsPer100g)
                .all { it.isFinite() && it >= 0.0 }

    fun toFoodItem(): FoodItem = original.copy(
        name = name,
        caloriesPer100g = caloriesPer100g,
        proteinPer100g = proteinPer100g,
        fatPer100g = fatPer100g,
        carbsPer100g = carbsPer100g
    ).toFoodItem()
}

data class PhotoEstimateUiState(
    val isLoading: Boolean = false,
    val items: List<EditableEstimate> = emptyList(),
    val error: String? = null
)

class PhotoEstimateViewModel(
    private val mealSlotId: Long,
    private val epochDay: Long,
    private val photoEstimationRepository: PhotoEstimationRepository,
    private val foodRepository: FoodRepository,
    private val nutritionLogRepository: NutritionLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoEstimateUiState())
    val uiState: StateFlow<PhotoEstimateUiState> = _uiState.asStateFlow()

    fun hasApiKey(): Boolean = photoEstimationRepository.hasApiKey()

    fun estimate(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, items = emptyList()) }
            photoEstimationRepository.estimateMeal(bitmap)
                .onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = items.map { dto -> EditableEstimate(dto) },
                            error = if (items.isEmpty()) "No food recognized in that photo — try again or add manually." else null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Estimation failed.") }
                }
        }
    }

    fun updateItem(index: Int, updated: EditableEstimate) {
        _uiState.update { state ->
            state.copy(items = state.items.toMutableList().also { it[index] = updated })
        }
    }

    fun removeItem(index: Int) {
        _uiState.update { state -> state.copy(items = state.items.filterIndexed { i, _ -> i != index }) }
    }

    fun logAll(onDone: () -> Unit) {
        viewModelScope.launch {
            val items = _uiState.value.items
            if (items.isEmpty() || items.any { !it.isValid }) return@launch
            items.forEach { editable ->
                val saved = foodRepository.saveAndUse(editable.toFoodItem())
                nutritionLogRepository.logFood(saved, mealSlotId, epochDay, editable.grams)
            }
            onDone()
        }
    }
}

private fun formatEstimateNumber(value: Double): String =
    if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
