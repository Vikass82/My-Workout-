package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WorkoutRepository
import com.example.data.model.UserProfile
import com.example.ui.WorkoutViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Workout Journey", appName)
    }

    @Test
    fun `repository database initialization and export test`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = WorkoutRepository(context)
        repository.ensureInitialized()

        val profile = repository.getUserProfileOnce()
        assertEquals(19, profile.age)
        assertEquals(45f, profile.currentWeightKg)
        assertEquals(65f, profile.targetWeightKg)

        // Test export JSON
        val exportedJson = repository.exportDataToJson()
        assertTrue(exportedJson.contains("My Workout Journey") || exportedJson.contains("profile"))
        assertTrue(exportedJson.contains("45"))

        // Test updating profile
        repository.updateUserProfile(profile.copy(name = "Alex", currentWeightKg = 46f))
        val updatedProfile = repository.getUserProfileOnce()
        assertEquals("Alex", updatedProfile.name)
        assertEquals(46f, updatedProfile.currentWeightKg)
    }

    @Test
    fun `all 11 exercises exist with accurate 3D demonstration keys`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = WorkoutRepository(context)
        repository.ensureInitialized()

        val exercises = repository.getAllExercises().first()
        assertTrue(exercises.isNotEmpty())

        val requiredKeys = setOf(
            "pushups",
            "diamond_pushups",
            "pike_pushups",
            "squats",
            "lunges",
            "calf_raises",
            "glute_bridge",
            "pullups",
            "rows",
            "curls",
            "bird_dog"
        )

        val presentKeys = exercises.map { it.illustrationKey.lowercase().trim() }.toSet()
        for (key in requiredKeys) {
            assertTrue("Expected exercise demonstration key '$key' to be present", presentKeys.contains(key))
        }

        // Verify each exercise has instructions, rest duration, and target sets
        exercises.forEach { ex ->
            assertTrue("Exercise ${ex.name} must have instructions", ex.instructions.isNotBlank())
            assertTrue("Exercise ${ex.name} must have positive rest", ex.restDurationSeconds > 0)
            assertTrue("Exercise ${ex.name} must have positive sets", ex.targetSets > 0)
            assertTrue("Exercise ${ex.name} must have valid reps min", ex.targetRepsMin > 0)
            assertTrue("Exercise ${ex.name} must have reps max >= min", ex.targetRepsMax >= ex.targetRepsMin)
        }
    }

    private suspend fun waitForCondition(timeoutMs: Long = 5000, condition: suspend () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition()) {
            ShadowLooper.idleMainLooper()
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw AssertionError("Condition timed out after $timeoutMs ms")
            }
            kotlinx.coroutines.delay(20)
        }
    }

    @Test
    fun `complete workout flow simulation from start to summary and history`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = WorkoutViewModel(app)
        viewModel.repository.ensureInitialized()

        // 1. START WORKOUT (Day 1 - Upper Body A)
        viewModel.startWorkout(dayOfWeek = 1)
        waitForCondition { viewModel.activeWorkout.value != null }
        var active = viewModel.activeWorkout.value
        assertNotNull("Active workout should not be null after start", active)
        assertEquals(1, active!!.dayOfWeek)
        assertEquals(0, active.currentExerciseIndex)
        assertTrue("Exercises for Day 1 must not be empty", active.exercises.isNotEmpty())

        val firstEx = active.exercises[0]
        assertEquals("Normal Push-ups", firstEx.name)

        // 2. ACTUAL REP ENTRY & SAVING SETS
        // Log set 1: 10 reps, Moderate difficulty
        viewModel.logSet(
            exercise = firstEx,
            actualReps = 10,
            actualRepsLeft = null,
            actualRepsRight = null,
            difficulty = "Moderate"
        )
        active = viewModel.activeWorkout.value!!
        assertEquals(1, active.loggedSets.size)
        assertEquals(10, active.loggedSets[0].actualReps)
        assertEquals(1, active.loggedSets[0].setNumber)

        // Log set 2: 12 reps, Hard difficulty
        viewModel.logSet(
            exercise = firstEx,
            actualReps = 12,
            actualRepsLeft = null,
            actualRepsRight = null,
            difficulty = "Hard"
        )
        active = viewModel.activeWorkout.value!!
        assertEquals(2, active.loggedSets.size)
        assertEquals(12, active.loggedSets[1].actualReps)
        assertEquals(2, active.loggedSets[1].setNumber)

        // 3. NEXT EXERCISE NAVIGATION
        viewModel.nextExercise()
        active = viewModel.activeWorkout.value!!
        assertEquals(1, active.currentExerciseIndex)
        val secondEx = active.exercises[1]

        // 4. LOG UNILATERAL REPS (if applicable) or standard reps on next exercise
        viewModel.logSet(
            exercise = secondEx,
            actualReps = 8,
            actualRepsLeft = 8,
            actualRepsRight = 8,
            difficulty = "Easy"
        )
        active = viewModel.activeWorkout.value!!
        assertEquals(3, active.loggedSets.size)

        // 5. SKIP AN EXERCISE
        viewModel.nextExercise()
        val thirdExIndex = viewModel.activeWorkout.value!!.currentExerciseIndex
        val thirdEx = viewModel.activeWorkout.value!!.exercises[thirdExIndex]
        viewModel.skipCurrentExercise()
        active = viewModel.activeWorkout.value!!
        assertTrue("Skipped set should contain third exercise id", active.skippedExerciseIds.contains(thirdEx.id))

        // 6. FINISH WORKOUT SESSION AND SAVE TO ROOM DATABASE
        var savedSessionId: Long? = null
        viewModel.finishWorkoutSession { id ->
            savedSessionId = id
        }
        waitForCondition { savedSessionId != null }

        // Wait for session to be saved into repository
        var savedSession = viewModel.repository.getAllSessions().first().firstOrNull()
        waitForCondition {
            savedSession = viewModel.repository.getAllSessions().first().firstOrNull()
            savedSession != null
        }
        assertNotNull("Saved session must exist in repository", savedSession)
        assertEquals(3, savedSession!!.totalSetsCompleted)
        assertEquals(30, savedSession!!.actualRepsTotal) // 10 + 12 + 8 = 30
        assertTrue("Session should be marked completed", savedSession!!.isCompleted)
        assertTrue("Skipped exercise should be in summary", savedSession!!.skippedExercisesList.contains(thirdEx.name))

        // 7. VERIFY SET LOGS IN PERSISTENT STORAGE
        val storedSets = viewModel.repository.getSetsForSessionOnce(savedSession!!.id)
        assertEquals(3, storedSets.size)
        assertEquals(10, storedSets[0].actualReps)
        assertEquals(12, storedSets[1].actualReps)
        assertEquals(8, storedSets[2].actualReps)

        // 8. VERIFY RECENT LOGS QUERY FOR PROGRESSIVE OVERLOAD
        val recentLogs = viewModel.repository.getRecentLogsForExercise(firstEx.id, 5)
        assertEquals(2, recentLogs.size)
        assertEquals(12, recentLogs[0].actualReps)

        // 9. CLEANUP WORKOUT SESSION IN HISTORY
        viewModel.deleteSession(savedSession!!.id)
        waitForCondition {
            viewModel.repository.getAllSessions().first().none { it.id == savedSession!!.id }
        }
        val remainingSets = viewModel.repository.getSetsForSessionOnce(savedSession!!.id)
        assertTrue("Sets must be cascaded on delete", remainingSets.isEmpty())
    }

    @Test
    fun `start workout fallback handles rest day gracefully without crash`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = WorkoutViewModel(app)
        viewModel.repository.ensureInitialized()

        // Starting a workout on a rest day (e.g. Day 3)
        viewModel.startWorkout(dayOfWeek = 3)
        waitForCondition { viewModel.activeWorkout.value != null }
        val active = viewModel.activeWorkout.value
        assertNotNull(active)
        assertTrue("Exercises must be populated even on rest day start", active!!.exercises.isNotEmpty())
    }
}
