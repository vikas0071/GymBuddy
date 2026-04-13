package com.vgroups.gymbuddy.domain.model

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

data class WorkoutDay(
    val label: String,         // e.g. "Push Day — Chest & Shoulders"
    val bodyParts: List<String> // keys matching free-exercise-db primaryMuscles
)

data class WorkoutSplit(
    val id: String,
    val name: String,
    val daysPerWeek: Int,
    val difficulty: Difficulty,
    val description: String,
    val days: List<WorkoutDay>
)

/** Pre-defined split catalogue — no network needed */
object WorkoutSplits {

    val all: List<WorkoutSplit> = listOf(

        WorkoutSplit(
            id = "push_pull_legs",
            name = "Push Pull Legs",
            daysPerWeek = 6,
            difficulty = Difficulty.INTERMEDIATE,
            description = "Science-backed split targeting each muscle twice/week. Best for hypertrophy.",
            days = listOf(
                WorkoutDay("Push Day A — Chest & Shoulders", listOf("chest", "shoulders", "triceps")),
                WorkoutDay("Pull Day A — Back & Biceps", listOf("middle back", "lats", "biceps")),
                WorkoutDay("Legs Day A — Quads & Glutes", listOf("quadriceps", "glutes", "calves")),
                WorkoutDay("Push Day B — Shoulders & Chest", listOf("shoulders", "chest", "triceps")),
                WorkoutDay("Pull Day B — Lats & Rear Delts", listOf("lats", "middle back", "biceps")),
                WorkoutDay("Legs Day B — Hamstrings & Calves", listOf("hamstrings", "glutes", "calves"))
            )
        ),

        WorkoutSplit(
            id = "upper_lower",
            name = "Upper Lower Split",
            daysPerWeek = 4,
            difficulty = Difficulty.BEGINNER,
            description = "Full upper + lower body twice a week. Great for beginners building a base.",
            days = listOf(
                WorkoutDay("Upper A — Chest, Back & Arms", listOf("chest", "lats", "biceps", "triceps")),
                WorkoutDay("Lower A — Quads & Hamstrings", listOf("quadriceps", "hamstrings", "glutes")),
                WorkoutDay("Upper B — Shoulders & Back", listOf("shoulders", "middle back", "biceps")),
                WorkoutDay("Lower B — Glutes, Hamstrings & Calves", listOf("glutes", "hamstrings", "calves"))
            )
        ),

        WorkoutSplit(
            id = "full_body",
            name = "Full Body",
            daysPerWeek = 3,
            difficulty = Difficulty.BEGINNER,
            description = "Hit every major muscle group every session. Perfect for 3-day week beginners.",
            days = listOf(
                WorkoutDay("Full Body A", listOf("chest", "middle back", "quadriceps", "shoulders")),
                WorkoutDay("Full Body B", listOf("lats", "hamstrings", "biceps", "triceps")),
                WorkoutDay("Full Body C", listOf("chest", "glutes", "shoulders", "abdominals"))
            )
        ),

        WorkoutSplit(
            id = "bro_split",
            name = "Bro Split",
            daysPerWeek = 5,
            difficulty = Difficulty.ADVANCED,
            description = "Classic bodybuilder split — each muscle group gets its own day of maximum volume.",
            days = listOf(
                WorkoutDay("Chest Day", listOf("chest", "triceps")),
                WorkoutDay("Back Day", listOf("lats", "middle back", "lower back")),
                WorkoutDay("Shoulder Day", listOf("shoulders", "traps")),
                WorkoutDay("Leg Day", listOf("quadriceps", "hamstrings", "glutes", "calves")),
                WorkoutDay("Arms Day", listOf("biceps", "triceps", "forearms"))
            )
        )
    )
}
