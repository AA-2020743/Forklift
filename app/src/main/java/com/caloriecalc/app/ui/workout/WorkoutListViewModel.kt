package com.caloriecalc.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.WorkoutSession
import com.caloriecalc.app.data.local.entity.WorkoutTemplate
import com.caloriecalc.app.data.repository.WorkoutRepository
import com.caloriecalc.app.data.repository.WorkoutTemplateRepository
import com.caloriecalc.app.domain.MuscleCoverageAnalyzer
import com.caloriecalc.app.domain.MuscleCoverageReport
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val workoutRepository: WorkoutRepository,
    private val workoutTemplateRepository: WorkoutTemplateRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    // 14 days rather than a strict 7 so splits that hit each muscle roughly weekly (or every
    // ~10 days) still get credit instead of being flagged every time the window resets.
    private val coverageWindowStart = today - 13

    val sessions: StateFlow<List<WorkoutSession>> = workoutRepository.observeSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<WorkoutTemplate>> = workoutTemplateRepository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coverageReport: StateFlow<MuscleCoverageReport?> = combine(
        workoutRepository.observeSetsInRange(coverageWindowStart, today),
        workoutRepository.observeExercises()
    ) { sets, exercises ->
        if (exercises.isEmpty()) null
        else MuscleCoverageAnalyzer.analyze(sets, exercises.associateBy { it.id }, exercises)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startSession(templateId: Long? = null, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = workoutRepository.startSession(LocalDate.now().toEpochDay(), templateId = templateId)
            onCreated(id)
        }
    }
}
