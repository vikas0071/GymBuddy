package com.vgroups.gymbuddy.presentation.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vgroups.gymbuddy.data.repository.ExerciseRepository
import com.vgroups.gymbuddy.domain.model.Exercise
import com.vgroups.gymbuddy.domain.model.WorkoutSplit
import com.vgroups.gymbuddy.domain.model.WorkoutSplits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExerciseUiState {
    object Loading : ExerciseUiState()
    data class Success(val exercises: List<Exercise>) : ExerciseUiState()
    data class Error(val message: String) : ExerciseUiState()
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val splitId: String = checkNotNull(savedStateHandle["splitId"])
    val dayIndex: Int = checkNotNull(savedStateHandle["dayIndex"])

    val split: WorkoutSplit = WorkoutSplits.all.first { it.id == splitId }
    val workoutDay = split.days[dayIndex]

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState: StateFlow<ExerciseUiState> = _uiState

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exercises = repository.getExercisesForDay(workoutDay.bodyParts)
                _uiState.value = ExerciseUiState.Success(exercises)
            } catch (e: Exception) {
                _uiState.value = ExerciseUiState.Error(e.message ?: "Failed to load exercises")
            }
        }
    }
}
