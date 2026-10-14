package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ... 7 = Sunday
    val name: String,
    val isRestDay: Boolean,
    val description: String,
    val focusArea: String
)
