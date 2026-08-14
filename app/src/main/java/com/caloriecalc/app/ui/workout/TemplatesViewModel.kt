package com.caloriecalc.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.WorkoutTemplate
import com.caloriecalc.app.data.repository.WorkoutTemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplatesViewModel(
    private val templateRepository: WorkoutTemplateRepository
) : ViewModel() {

    val templates: StateFlow<List<WorkoutTemplate>> = templateRepository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTemplate(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = templateRepository.createTemplate(name)
            onCreated(id)
        }
    }

    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch { templateRepository.deleteTemplate(template) }
    }
}
