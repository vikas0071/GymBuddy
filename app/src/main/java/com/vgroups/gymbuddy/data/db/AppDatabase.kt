package com.vgroups.gymbuddy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SelectedSplitEntity::class, WorkoutHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun splitDao(): SplitDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
}
