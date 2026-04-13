package com.vgroups.gymbuddy.data.api

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the JSON schema of free-exercise-db exercises.json
 * Images field: ["ExerciseName/0.jpg", "ExerciseName/1.jpg"]
 */
data class ExerciseDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("force") val force: String?,
    @SerializedName("level") val level: String,
    @SerializedName("mechanic") val mechanic: String?,
    @SerializedName("equipment") val equipment: String?,
    @SerializedName("primaryMuscles") val primaryMuscles: List<String>,
    @SerializedName("secondaryMuscles") val secondaryMuscles: List<String>,
    @SerializedName("instructions") val instructions: List<String>,
    @SerializedName("category") val category: String,
    @SerializedName("images") val images: List<String>
) {
    /** Build URL for first image using the raw GitHub CDN */
    /** Build URL for the primary exercise image using the raw GitHub CDN */
    fun imageUrl(): String {
        return if (images.isNotEmpty()) {
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/${images[0]}"
        } else {
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/$id/0.jpg"
        }
    }
}
