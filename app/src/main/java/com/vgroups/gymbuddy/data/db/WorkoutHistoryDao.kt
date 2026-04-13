package com.vgroups.gymbuddy.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutHistoryEntity)

    @Query("SELECT * FROM workout_history ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<WorkoutHistoryEntity>>

    @Query("SELECT COUNT(*) FROM workout_history")
    suspend fun getTotalSessions(): Int
}
