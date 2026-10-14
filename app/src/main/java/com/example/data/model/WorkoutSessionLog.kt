package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_session_logs")
data class WorkoutSessionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planName: String,
    val dayOfWeek: Int,
    val dateEpochMillis: Long,
    val durationSeconds: Int,
    val plannedExercisesCount: Int,
    val completedExercisesCount: Int,
    val totalSetsCompleted: Int,
    val plannedRepsTotal: Int,
    val actualRepsTotal: Int,
    val isCompleted: Boolean = true,
    val skippedExercisesList: String = "", // Comma-separated names of skipped exercises
    val notes: String = ""
)
