package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 19,
    val heightCm: Float = 182f,
    val currentWeightKg: Float = 45f,
    val targetWeightKg: Float = 65f,
    val experience: String = "Some exercise experience, new to structured sets",
    val equipment: String = "Pull-up bar, chair, bodyweight, school bag",
    val daysPerWeek: Int = 4,
    val primaryGoal: String = "Gain weight and muscle at home without a gym",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val bmi: Float
        get() {
            val heightM = heightCm / 100f
            return if (heightM > 0) currentWeightKg / (heightM * heightM) else 0f
        }

    val isBmiVeryLow: Boolean
        get() = bmi in 0.1f..16.5f
}
