package com.caloriecalc.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.SetEntry
import com.caloriecalc.app.data.local.entity.WorkoutSession
import com.caloriecalc.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseSetGroup(val exercise: Exercise, val sets: List<SetEntry>)

class WorkoutSessionViewModel(
    private val sessionId: Long,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _session = MutableStateFlow<WorkoutSession?>(null)
    val session: StateFlow<WorkoutSession?> = _session.asStateFlow()

    val groupedSets: StateFlow<List<ExerciseSetGroup>> = combine(
        workoutRepository.observeSetsForSession(sessionId),
        workoutRepository.observeExercises()
    ) { sets, exercises ->
        val byId = exercises.associateBy { it.id }
        sets.groupBy { it.exerciseId }
            .mapNotNull { (exerciseId, setsForExercise) ->
                byId[exerciseId]?.let { ExerciseSetGroup(it, setsForExercise.sortedBy { s -> s.setNumber }) }
            }
            .sortedBy { group -> group.sets.minOf { it.id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _session.value = workoutRepository.getSession(sessionId)
        }
    }

    fun deleteSet(set: SetEntry) {
        viewModelScope.launch { workoutRepository.deleteSet(set) }
    }
}
