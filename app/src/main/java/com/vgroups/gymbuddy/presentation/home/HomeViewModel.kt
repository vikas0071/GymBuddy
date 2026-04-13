package com.vgroups.gymbuddy.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vgroups.gymbuddy.data.repository.ExerciseRepository
import com.vgroups.gymbuddy.domain.model.WorkoutSplit
import com.vgroups.gymbuddy.domain.model.WorkoutSplits
import com.vgroups.gymbuddy.domain.repository.ProgressRepository
import com.vgroups.gymbuddy.domain.model.WorkoutDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutSuggestion(
    val split: WorkoutSplit,
    val dayIndex: Int,
    val day: WorkoutDay
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExerciseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    val splits: List<WorkoutSplit> = WorkoutSplits.all

    private val _lastSplitId = MutableStateFlow<String?>(null)
    val lastSplitId: StateFlow<String?> = _lastSplitId

    private val _suggestion = MutableStateFlow<WorkoutSuggestion?>(null)
    val suggestion: StateFlow<WorkoutSuggestion?> = _suggestion

    init {
        viewModelScope.launch {
            _lastSplitId.value = repository.getSelectedSplitId()
            
            // Fetch next suggested workout
            progressRepository.getNextSuggestedWorkout()?.let { (split, index, _) ->
                _suggestion.value = WorkoutSuggestion(split, index, split.days[index])
            }
        }
    }

    fun onSplitSelected(splitId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.saveSelectedSplit(splitId)
            _lastSplitId.value = splitId
            onDone()
        }
    }
}
