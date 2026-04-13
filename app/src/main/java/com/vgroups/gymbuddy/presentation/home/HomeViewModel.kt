package com.vgroups.gymbuddy.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vgroups.gymbuddy.data.repository.ExerciseRepository
import com.vgroups.gymbuddy.domain.model.WorkoutSplit
import com.vgroups.gymbuddy.domain.model.WorkoutSplits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExerciseRepository
) : ViewModel() {

    // All available splits are static — no loading state needed
    val splits: List<WorkoutSplit> = WorkoutSplits.all

    private val _lastSplitId = MutableStateFlow<String?>(null)
    val lastSplitId: StateFlow<String?> = _lastSplitId

    init {
        viewModelScope.launch {
            _lastSplitId.value = repository.getSelectedSplitId()
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
