package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_definitions")
data class ExerciseDefinition(
    @PrimaryKey val id: String, // e.g., "mon_pushups"
    val dayOfWeek: Int, // 1 to 7
    val orderIndex: Int,
    val name: String,
    val targetSets: Int,
    val targetRepsMin: Int,
    val targetRepsMax: Int,
    val restDurationSeconds: Int,
    val isUnilateral: Boolean = false,
    val unit: String = "reps",
    val instructions: String,
    val postureAndBreathing: String,
    val commonMistakes: String,
    val alternativeName: String,
    val alternativeDescription: String,
    val illustrationKey: String = "pushups"
)
