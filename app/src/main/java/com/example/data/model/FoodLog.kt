package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val foodItem: String,
    val quantity: String,
    val calories: Int? = null,
    val proteinGrams: Float? = null,
    val notes: String = "",
    val dateEpochMillis: Long = System.currentTimeMillis()
)
