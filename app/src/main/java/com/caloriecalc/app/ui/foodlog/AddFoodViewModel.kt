package com.caloriecalc.app.ui.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.dao.MealTemplateSummary
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.repository.FoodRepository
import com.caloriecalc.app.data.repository.MealTemplateRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AddFoodViewModel(
    private val foodRepository: FoodRepository,
    private val mealTemplateRepository: MealTemplateRepository
) : ViewModel() {

    val templates: StateFlow<List<MealTemplateSummary>> = mealTemplateRepository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val localResults: StateFlow<List<FoodItem>> = _query
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else foodRepository.searchLocal(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recent: StateFlow<List<FoodItem>> = foodRepository.recentlyUsed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val frequent: StateFlow<List<FoodItem>> = foodRepository.frequentlyUsed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _onlineResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val onlineResults: StateFlow<List<FoodItem>> = _onlineResults.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        _onlineResults.value = emptyList()
    }

    fun searchOnline() {
        val q = _query.value
        if (q.isBlank()) return
        viewModelScope.launch {
            _isSearchingOnline.value = true
            _onlineResults.value = foodRepository.searchOnline(q)
            _isSearchingOnline.value = false
        }
    }

    suspend fun selectFood(food: FoodItem): FoodItem =
        if (food.id == 0L) foodRepository.saveAndUse(food) else {
            foodRepository.markUsed(food.id)
            food
        }

    suspend fun applyTemplate(templateId: Long, mealSlotId: Long, epochDay: Long) {
        mealTemplateRepository.applyTemplate(templateId, mealSlotId, epochDay)
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch { mealTemplateRepository.deleteTemplate(id) }
    }
}
