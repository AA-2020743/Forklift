package com.caloriecalc.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.SetEntry
import com.caloriecalc.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogSetViewModel(
    private val sessionId: Long,
    private val exerciseId: Long,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    val sets: StateFlow<List<SetEntry>> = workoutRepository.observeSetsForSession(sessionId)
        .map { list -> list.filter { it.exerciseId == exerciseId }.sortedBy { it.setNumber } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _exercise.value = workoutRepository.getExerciseById(exerciseId)
        }
    }

    fun addSet(weightKg: Double, reps: Int) {
        viewModelScope.launch {
            val nextSetNumber = sets.value.size + 1
            workoutRepository.addSet(
                SetEntry(
                    workoutSessionId = sessionId,
                    exerciseId = exerciseId,
                    setNumber = nextSetNumber,
                    weightKg = weightKg,
                    reps = reps
                )
            )
        }
    }

    fun deleteSet(set: SetEntry) {
        viewModelScope.launch { workoutRepository.deleteSet(set) }
    }
}
