package com.caloriecalc.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.WorkoutTemplate
import com.caloriecalc.app.data.repository.TemplateExerciseWithDetails
import com.caloriecalc.app.data.repository.WorkoutTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateEditorViewModel(
    private val templateId: Long,
    private val templateRepository: WorkoutTemplateRepository
) : ViewModel() {

    private val _template = MutableStateFlow<WorkoutTemplate?>(null)
    val template: StateFlow<WorkoutTemplate?> = _template.asStateFlow()

    val exercises: StateFlow<List<TemplateExerciseWithDetails>> =
        templateRepository.observeTemplateExercises(templateId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _template.value = templateRepository.getTemplate(templateId)
        }
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch { templateRepository.addExercise(templateId, exerciseId) }
    }

    fun removeExercise(templateExerciseId: Long) {
        viewModelScope.launch { templateRepository.removeExercise(templateExerciseId) }
    }
}
