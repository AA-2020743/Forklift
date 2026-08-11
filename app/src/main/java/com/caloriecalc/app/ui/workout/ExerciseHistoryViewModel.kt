package com.caloriecalc.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.dao.SetWithSessionDate
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseHistoryViewModel(
    private val exerciseId: Long,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    val history: StateFlow<List<SetWithSessionDate>> = workoutRepository.observeExerciseHistory(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _exercise.value = workoutRepository.getExerciseById(exerciseId)
        }
    }
}
