package com.vgroups.gymbuddy.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vgroups.gymbuddy.data.api.ExerciseDto
import com.vgroups.gymbuddy.data.db.SelectedSplitEntity
import com.vgroups.gymbuddy.data.db.SplitDao
import com.vgroups.gymbuddy.data.db.WorkoutHistoryDao
import com.vgroups.gymbuddy.data.db.WorkoutHistoryEntity
import com.vgroups.gymbuddy.domain.model.Exercise
import com.vgroups.gymbuddy.domain.model.WorkoutSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val splitDao: SplitDao,
    private val historyDao: WorkoutHistoryDao
) {
    // Lazily parse the bundled JSON once and cache in memory
    private val allExercises: List<ExerciseDto> by lazy {
        val json = context.assets.open("exercises.json")
            .bufferedReader()
            .use { it.readText() }
        val type = object : TypeToken<List<ExerciseDto>>() {}.type
        gson.fromJson(json, type)
    }

    /**
     * Returns 8–10 exercises for the given list of muscle groups.
     * Picks at least 1 exercise per bodyPart, then fills to 10 with category "strength".
     */
    fun getExercisesForDay(bodyParts: List<String>): List<Exercise> {
        val strengthOnly = allExercises.filter { it.category == "strength" }
        val result = mutableListOf<ExerciseDto>()

        // Pick 2-3 exercises per primary body part
        for (part in bodyParts) {
            val matching = strengthOnly.filter { dto ->
                dto.primaryMuscles.any { m -> m.equals(part, ignoreCase = true) }
            }.shuffled().take(2)
            result.addAll(matching)
        }

        // Deduplicate and cap at 10
        val unique = result.distinctBy { it.id }.take(10)

        // If we ended up with fewer than 8, top up from the first bodyPart
        val topped = if (unique.size < 8) {
            val extra = strengthOnly
                .filter { e -> unique.none { u -> u.id == e.id } }
                .filter { dto ->
                    dto.primaryMuscles.any { m ->
                        bodyParts.any { bp -> m.equals(bp, ignoreCase = true) }
                    }
                }
                .take(8 - unique.size)
            unique + extra
        } else unique

        return topped.mapIndexed { index, dto -> dto.toDomain(index) }
    }

    // ── Room persistence ──────────────────────────────────────────────────────

    suspend fun saveSelectedSplit(splitId: String) {
        splitDao.saveSelectedSplit(SelectedSplitEntity(splitId = splitId))
    }

    suspend fun getSelectedSplitId(): String? = splitDao.getSelectedSplit()?.splitId

    suspend fun saveWorkoutSession(session: WorkoutSession) {
        historyDao.insertSession(
            WorkoutHistoryEntity(
                splitId = session.splitId,
                dayIndex = session.dayIndex,
                splitName = session.splitName,
                dayLabel = session.dayLabel,
                totalExercises = session.totalExercises,
                durationSeconds = session.durationSeconds,
                completedAt = session.completedAt
            )
        )
    }

    fun getWorkoutHistory() = historyDao.getAllSessions()
}

// ── Mapping ───────────────────────────────────────────────────────────────────

private fun ExerciseDto.toDomain(index: Int): Exercise {
    // Assign sets/reps based on position in workout and category
    val (sets, reps) = when (index) {
        0 -> Pair(4, "6-8")   // Compound opener: heavier
        1 -> Pair(4, "8-10")
        2 -> Pair(3, "10-12")
        3 -> Pair(3, "10-12")
        else -> Pair(3, "12-15") // Accessory: lighter, higher reps
    }

    return Exercise(
        id = id,
        name = name,
        primaryMuscle = primaryMuscles.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "General",
        secondaryMuscles = secondaryMuscles,
        equipment = equipment ?: "Body Only",
        level = level.replaceFirstChar { it.uppercase() },
        category = category,
        instructions = instructions,
        imageUrl = imageUrl(),
        sets = sets,
        reps = reps
    )
}
