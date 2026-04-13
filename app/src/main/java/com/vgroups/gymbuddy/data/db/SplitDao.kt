package com.vgroups.gymbuddy.data.db

import androidx.room.*

@Dao
interface SplitDao {
    @Query("SELECT * FROM selected_split WHERE `key` = 'current' LIMIT 1")
    suspend fun getSelectedSplit(): SelectedSplitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSelectedSplit(entity: SelectedSplitEntity)
}
