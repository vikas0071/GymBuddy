package com.vgroups.gymbuddy.domain.repository

import com.vgroups.gymbuddy.data.db.WorkoutHistoryDao
import com.vgroups.gymbuddy.domain.model.WorkoutSplit
import com.vgroups.gymbuddy.domain.model.WorkoutSplits
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

import java.util.Calendar

@Singleton
class ProgressRepository @Inject constructor(
    private val workoutHistoryDao: WorkoutHistoryDao
) {
    /**
     * Maps the current day of building to a split day index.
     * Returns Triple(Split, DayIndex, isRestDay)
     */
    suspend fun getTodayWorkout(splitId: String?): Triple<WorkoutSplit, Int, Boolean>? {
        val targetSplitId = splitId ?: "push_pull_legs" // Default to PPL if none selected
        val split = WorkoutSplits.all.find { it.id == targetSplitId } ?: return null

        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Monday = 0, Tuesday = 1 ..., Sunday = 6
        val weekdayIndex = when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        // If the split doesn't have a workout for this week-day index, it's a rest day
        val isRestDay = weekdayIndex >= split.days.size

        return Triple(split, weekdayIndex, isRestDay)
    }

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
