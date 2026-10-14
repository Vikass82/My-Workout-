package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_set_logs")
data class WorkoutSetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val targetReps: Int,
    val actualReps: Int,
    val actualRepsLeft: Int? = null,
    val actualRepsRight: Int? = null,
    val difficulty: String = "Moderate", // Easy, Moderate, Hard
    val isSkipped: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
