package com.vgroups.gymbuddy.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val primaryMuscle: String,
    val secondaryMuscles: List<String>,
    val equipment: String,
    val level: String,
    val category: String,
    val instructions: List<String>,
    // GIF/image URL from free-exercise-db
    val imageUrl: String,
    // Workout prescription
    val sets: Int,
    val reps: String    // e.g. "8-12" or "12-15"
)
