package com.vgroups.gymbuddy.di

import android.content.Context
import androidx.room.Room
import com.vgroups.gymbuddy.data.db.AppDatabase
import com.vgroups.gymbuddy.data.db.SplitDao
import com.vgroups.gymbuddy.data.db.WorkoutHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "gymbuddy.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideSplitDao(db: AppDatabase): SplitDao = db.splitDao()

    @Provides
    fun provideWorkoutHistoryDao(db: AppDatabase): WorkoutHistoryDao = db.workoutHistoryDao()
}
