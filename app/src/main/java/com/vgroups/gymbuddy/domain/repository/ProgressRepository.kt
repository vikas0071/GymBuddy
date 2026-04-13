package com.vgroups.gymbuddy.domain.repository

import com.vgroups.gymbuddy.data.db.WorkoutHistoryDao
import com.vgroups.gymbuddy.domain.model.WorkoutSplit
import com.vgroups.gymbuddy.domain.model.WorkoutSplits
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val workoutHistoryDao: WorkoutHistoryDao
) {
    /**
     * Finds the next suggested workout day based on history.
     * Returns Triple(Split, DayIndex, isSuggested)
     */
    suspend fun getNextSuggestedWorkout(): Triple<WorkoutSplit, Int, Boolean>? {
        val history = workoutHistoryDao.getAllSessions().first()
        if (history.isEmpty()) return null

        val lastSession = history.first()
        val split = WorkoutSplits.all.find { it.id == lastSession.splitId } ?: return null

        // If last split had 3 days, and we did day 2, next is (2 + 1) % 3 = 0
        val nextDayIndex = (lastSession.dayIndex + 1) % split.days.size

        return Triple(split, nextDayIndex, true)
    }
}
