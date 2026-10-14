package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultWorkoutData
import com.example.data.WorkoutMetrics
import com.example.data.WorkoutRepository
import com.example.data.model.ExerciseDefinition
import com.example.data.model.FoodLog
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutSessionLog
import com.example.data.model.WorkoutSetLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ActiveWorkoutState(
    val dayOfWeek: Int = 1,
    val planName: String = "",
    val exercises: List<ExerciseDefinition> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val loggedSets: List<WorkoutSetLog> = emptyList(),
    val skippedExerciseIds: Set<String> = emptySet(),
    val startEpochMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val savedSessionId: Long? = null
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    val repository = WorkoutRepository(application)

    // Persistent StateFlows
    val userProfile: StateFlow<UserProfile> = repository.getUserProfile()
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DefaultWorkoutData.defaultProfile
        )

    val allPlans: StateFlow<List<WorkoutPlan>> = repository.getAllPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DefaultWorkoutData.defaultPlans
        )

    val allExercises: StateFlow<List<ExerciseDefinition>> = repository.getAllExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DefaultWorkoutData.defaultExercises
        )

    val allSessions: StateFlow<List<WorkoutSessionLog>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lastCompletedSession: StateFlow<WorkoutSessionLog?> = repository.getLastCompletedSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allWeightLogs: StateFlow<List<WeightLog>> = repository.getAllWeightLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(DefaultWorkoutData.defaultInitialWeight)
        )

    val allFoodLogs: StateFlow<List<FoodLog>> = repository.getAllFoodLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _metrics = MutableStateFlow(WorkoutMetrics())
    val metrics: StateFlow<WorkoutMetrics> = _metrics.asStateFlow()

    // Active Workout Session State
    private val _activeWorkout = MutableStateFlow<ActiveWorkoutState?>(null)
    val activeWorkout: StateFlow<ActiveWorkoutState?> = _activeWorkout.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            refreshMetrics()
        }
    }

    fun refreshMetrics() {
        viewModelScope.launch {
            _metrics.value = repository.calculateMetrics()
        }
    }

    // Profile
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
        }
    }

    // Exercises
    fun updateExercise(exercise: ExerciseDefinition) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    // Weight
    fun addWeightLog(weightKg: Float, dateMillis: Long = System.currentTimeMillis(), notes: String = "") {
        viewModelScope.launch {
            repository.addWeightLog(weightKg, dateMillis, notes)
        }
    }

    fun deleteWeightLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWeightLog(id)
        }
    }

    // Food
    fun addFoodLog(mealType: String, foodItem: String, quantity: String, calories: Int?, protein: Float?, notes: String) {
        viewModelScope.launch {
            repository.addFoodLog(
                FoodLog(
                    mealType = mealType,
                    foodItem = foodItem,
                    quantity = quantity,
                    calories = calories,
                    proteinGrams = protein,
                    notes = notes
                )
            )
        }
    }

    fun deleteFoodLog(id: Long) {
        viewModelScope.launch {
            repository.deleteFoodLog(id)
        }
    }

    // Workout History Actions
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            refreshMetrics()
        }
    }

    // Start Workout Session
    fun startWorkout(dayOfWeek: Int) {
        viewModelScope.launch {
            val plan = repository.getPlanForDayOnce(dayOfWeek) ?: DefaultWorkoutData.defaultPlans.firstOrNull { it.dayOfWeek == dayOfWeek }
            var exercises = repository.getExercisesForDayOnce(dayOfWeek)
            if (exercises.isEmpty()) {
                exercises = DefaultWorkoutData.defaultExercises.filter { it.dayOfWeek == dayOfWeek }.ifEmpty {
                    DefaultWorkoutData.defaultExercises.filter { it.dayOfWeek == 1 }
                }
            }
            _activeWorkout.value = ActiveWorkoutState(
                dayOfWeek = dayOfWeek,
                planName = plan?.name ?: "Workout Day $dayOfWeek",
                exercises = exercises,
                currentExerciseIndex = 0,
                loggedSets = emptyList(),
                skippedExerciseIds = emptySet(),
                startEpochMillis = System.currentTimeMillis(),
                durationSeconds = 0,
                isPaused = false,
                isFinished = false
            )
        }
    }

    fun tickWorkoutTimer() {
        val current = _activeWorkout.value ?: return
        if (!current.isPaused && !current.isFinished) {
            _activeWorkout.value = current.copy(durationSeconds = current.durationSeconds + 1)
        }
    }

    fun toggleWorkoutPause() {
        val current = _activeWorkout.value ?: return
        _activeWorkout.value = current.copy(isPaused = !current.isPaused)
    }

    fun logSet(
        exercise: ExerciseDefinition,
        actualReps: Int,
        actualRepsLeft: Int?,
        actualRepsRight: Int?,
        difficulty: String
    ) {
        val current = _activeWorkout.value ?: return
        val existingSetsForThis = current.loggedSets.filter { it.exerciseId == exercise.id }
        val newSetNumber = existingSetsForThis.size + 1

        val newSetLog = WorkoutSetLog(
            sessionId = 0, // Assigned upon session save
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            setNumber = newSetNumber,
            targetReps = exercise.targetRepsMax,
            actualReps = actualReps,
            actualRepsLeft = actualRepsLeft,
            actualRepsRight = actualRepsRight,
            difficulty = difficulty,
            isSkipped = false,
            timestamp = System.currentTimeMillis()
        )

        _activeWorkout.value = current.copy(
            loggedSets = current.loggedSets + newSetLog
        )
    }

    fun skipCurrentExercise() {
        val current = _activeWorkout.value ?: return
        val exercise = current.exercises.getOrNull(current.currentExerciseIndex) ?: return
        _activeWorkout.value = current.copy(
            skippedExerciseIds = current.skippedExerciseIds + exercise.id,
            currentExerciseIndex = (current.currentExerciseIndex + 1).coerceAtMost(current.exercises.size)
        )
    }

    fun nextExercise() {
        val current = _activeWorkout.value ?: return
        _activeWorkout.value = current.copy(
            currentExerciseIndex = (current.currentExerciseIndex + 1).coerceAtMost(current.exercises.size)
        )
    }

    fun previousExercise() {
        val current = _activeWorkout.value ?: return
        _activeWorkout.value = current.copy(
            currentExerciseIndex = (current.currentExerciseIndex - 1).coerceAtLeast(0)
        )
    }

    fun finishWorkoutSession(onSaved: (sessionId: Long) -> Unit) {
        val current = _activeWorkout.value ?: return
        if (current.savedSessionId != null) {
            // Already saved, prevent duplicates
            onSaved(current.savedSessionId)
            return
        }

        viewModelScope.launch {
            val plannedExercisesCount = current.exercises.size
            val completedExercises = current.exercises.filter { ex ->
                current.loggedSets.any { it.exerciseId == ex.id && !it.isSkipped }
            }
            val skippedNames = current.exercises
                .filter { current.skippedExerciseIds.contains(it.id) }
                .joinToString(", ") { it.name }

            val plannedReps = current.exercises.sumOf { it.targetSets * it.targetRepsMax }
            val actualReps = current.loggedSets.filter { !it.isSkipped }.sumOf { it.actualReps }

            val session = WorkoutSessionLog(
                planName = current.planName,
                dayOfWeek = current.dayOfWeek,
                dateEpochMillis = System.currentTimeMillis(),
                durationSeconds = current.durationSeconds,
                plannedExercisesCount = plannedExercisesCount,
                completedExercisesCount = completedExercises.size,
                totalSetsCompleted = current.loggedSets.filter { !it.isSkipped }.size,
                plannedRepsTotal = plannedReps,
                actualRepsTotal = actualReps,
                isCompleted = completedExercises.isNotEmpty(),
                skippedExercisesList = skippedNames,
                notes = ""
            )

            val sessionId = repository.saveSession(session)

            // Save sets associated with this session
            current.loggedSets.forEach { setLog ->
                repository.saveSetLog(setLog.copy(sessionId = sessionId))
            }

            _activeWorkout.value = current.copy(
                isFinished = true,
                savedSessionId = sessionId
            )

            refreshMetrics()
            onSaved(sessionId)
        }
    }

    fun discardOrExitWorkout() {
        _activeWorkout.value = null
    }

    // Export & Import
    suspend fun exportDataJson(): String {
        return repository.exportDataToJson()
    }

    suspend fun importDataJson(jsonStr: String): Boolean {
        val success = repository.importDataFromJson(jsonStr)
        if (success) {
            refreshMetrics()
        }
        return success
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            refreshMetrics()
        }
    }
}
