package com.vgroups.gymbuddy.domain.model

data class WorkoutSession(
    val splitId: String,
    val splitName: String,
    val dayLabel: String,
    val totalExercises: Int,
    val durationSeconds: Long,
    val completedAt: Long = System.currentTimeMillis()
)
