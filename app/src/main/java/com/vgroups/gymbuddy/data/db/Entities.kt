package com.vgroups.gymbuddy.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selected_split")
data class SelectedSplitEntity(
    @PrimaryKey val key: String = "current",
    val splitId: String
)

@Entity(tableName = "workout_history")
data class WorkoutHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitId: String,
    val splitName: String,
    val dayLabel: String,
    val dayIndex: Int,
    val totalExercises: Int,
    val durationSeconds: Long,
    val completedAt: Long
)
