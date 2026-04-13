package com.vgroups.gymbuddy.data.api

import com.vgroups.gymbuddy.data.api.ExerciseDto
import retrofit2.http.GET

/**
 * Retrofit service to load exercises.json bundled via a local assets server
 * OR from the GitHub raw CDN if internet is available.
 * The base URL is configured in NetworkModule to point to the local asset file.
 */
interface ExerciseApiService {
    /**
     * Fetches the full exercise list from the bundled local JSON or remote CDN.
     */
    @GET("exercises.json")
    suspend fun getExercises(): List<ExerciseDto>
}
