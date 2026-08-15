package com.caloriecalc.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.SetEntry
import com.caloriecalc.app.data.local.entity.WorkoutSession
import com.caloriecalc.app.data.repository.WorkoutRepository
import com.caloriecalc.app.data.repository.WorkoutTemplateRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExerciseSetGroup(val exercise: Exercise, val sets: List<SetEntry>)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutSessionViewModel(
    private val sessionId: Long,
    private val workoutRepository: WorkoutRepository,
    private val workoutTemplateRepository: WorkoutTemplateRepository
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

    /** Exercises from the session's template that haven't had any sets logged yet this session. */
    val plannedExercises: StateFlow<List<Exercise>> = combine(
        _session.flatMapLatest { session ->
            val templateId = session?.templateId
            if (templateId != null) workoutTemplateRepository.observeTemplateExercises(templateId) else flowOf(emptyList())
        },
        groupedSets
    ) { templateExercises, groups ->
        val loggedIds = groups.map { it.exercise.id }.toSet()
        templateExercises.map { it.exercise }.filter { it.id !in loggedIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _session.value = workoutRepository.getSession(sessionId)
        }
    }

    fun deleteSet(set: SetEntry) {
        viewModelScope.launch { workoutRepository.deleteSet(set) }
    }

    fun finishSession() {
        viewModelScope.launch {
            workoutRepository.finishSession(sessionId)
            _session.value = workoutRepository.getSession(sessionId)
        }
    }
}
