package com.vgroups.gymbuddy.presentation.celebration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vgroups.gymbuddy.data.db.WorkoutHistoryDao
import com.vgroups.gymbuddy.data.db.WorkoutHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CelebrationViewModel @Inject constructor(
    private val workoutHistoryDao: WorkoutHistoryDao
) : ViewModel() {

    private var hasSaved = false

    fun saveWorkout(
        splitId: String,
        dayIndex: Int,
        splitName: String,
        dayLabel: String,
        exerciseCount: Int,
        durationSeconds: Long
    ) {
        if (hasSaved) return
        hasSaved = true

        viewModelScope.launch {
            val entity = WorkoutHistoryEntity(
                splitId = splitId,
                dayIndex = dayIndex,
                splitName = splitName,
                dayLabel = dayLabel,
                totalExercises = exerciseCount,
                durationSeconds = durationSeconds,
                completedAt = System.currentTimeMillis()
            )
            workoutHistoryDao.insertSession(entity)
        }
    }
}
