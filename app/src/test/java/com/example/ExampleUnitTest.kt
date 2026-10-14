package com.example

import com.example.data.DefaultWorkoutData
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun userProfile_bmiCalculationAndUnderweightNotice() {
        val initialProfile = UserProfile(
            age = 19,
            heightCm = 182f,
            currentWeightKg = 45f,
            targetWeightKg = 65f
        )
        // BMI = 45 / (1.82^2) = 45 / 3.3124 = 13.585
        assertTrue(initialProfile.bmi in 13.5f..13.7f)
        assertTrue(initialProfile.isBmiVeryLow)

        val targetProfile = initialProfile.copy(currentWeightKg = 65f)
        // BMI = 65 / (1.82^2) = 19.62
        assertTrue(targetProfile.bmi in 19.5f..19.8f)
        assertFalse(targetProfile.isBmiVeryLow)
    }

    @Test
    fun defaultWorkoutPlan_containsStructuredFourDays() {
        val plans = DefaultWorkoutData.defaultPlans
        assertEquals(7, plans.size)

        // Mon, Tue, Thu, Fri active days
        val activePlans = plans.filter { !it.isRestDay }
        assertEquals(4, activePlans.size)

        val exercises = DefaultWorkoutData.defaultExercises
        val mondayEx = exercises.filter { it.dayOfWeek == 1 }
        assertEquals(4, mondayEx.size)
        assertTrue(mondayEx.any { it.name == "Normal Push-ups" })
        assertTrue(mondayEx.any { it.name == "Pike Push-ups" })
        assertTrue(mondayEx.any { it.name == "Diamond Push-ups" })
        assertTrue(mondayEx.any { it.name == "School-bag Rows" })

        val tuesdayEx = exercises.filter { it.dayOfWeek == 2 }
        assertEquals(4, tuesdayEx.size)
        assertTrue(tuesdayEx.any { it.name == "Reverse Lunges" && it.isUnilateral })

        val thursdayEx = exercises.filter { it.dayOfWeek == 4 }
        assertEquals(4, thursdayEx.size)
        assertTrue(thursdayEx.any { it.name == "Pull-ups" })

        val fridayEx = exercises.filter { it.dayOfWeek == 5 }
        assertEquals(5, fridayEx.size)
        assertTrue(fridayEx.any { it.name == "Bird-dog" && it.isUnilateral })
    }
}
