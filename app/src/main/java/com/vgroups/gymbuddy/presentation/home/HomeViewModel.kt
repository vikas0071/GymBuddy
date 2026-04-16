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
    val day: WorkoutDay?,
    val isRestDay: Boolean,
    val dayName: String
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
        refreshTodayGoal()
    }

    private fun refreshTodayGoal() {
        viewModelScope.launch {
            val splitId = repository.getSelectedSplitId()
            _lastSplitId.value = splitId
            
            // Map weekday name
            val dayName = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
                .format(java.util.Date())

            // Fetch today's workout based on calendar
            progressRepository.getTodayWorkout(splitId)?.let { (split, index, isRest) ->
                _suggestion.value = WorkoutSuggestion(
                    split = split,
                    dayIndex = index,
                    day = if (isRest) null else split.days.getOrNull(index),
                    isRestDay = isRest,
                    dayName = dayName
                )
            }
        }
    }

    fun onSplitSelected(splitId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.saveSelectedSplit(splitId)
            _lastSplitId.value = splitId
            refreshTodayGoal() // Refresh goal for the new split
            onDone()
        }
    }
}
