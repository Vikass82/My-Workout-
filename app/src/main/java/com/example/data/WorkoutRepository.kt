package com.example.data

import android.content.Context
import com.example.data.model.ExerciseDefinition
import com.example.data.model.FoodLog
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutSessionLog
import com.example.data.model.WorkoutSetLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class WorkoutRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val profileDao = database.userProfileDao()
    private val planDao = database.workoutPlanDao()
    private val exerciseDao = database.exerciseDao()
    private val workoutLogDao = database.workoutLogDao()
    private val weightDao = database.weightLogDao()
    private val foodDao = database.foodLogDao()

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        populateInitialData(database)
    }

    // Profile
    fun getUserProfile(): Flow<UserProfile?> = profileDao.getUserProfile()
    suspend fun getUserProfileOnce(): UserProfile = withContext(Dispatchers.IO) {
        profileDao.getUserProfileOnce() ?: DefaultWorkoutData.defaultProfile
    }
    suspend fun updateUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.insertOrUpdate(profile)
    }

    // Plans
    fun getAllPlans(): Flow<List<WorkoutPlan>> = planDao.getAllPlans()
    suspend fun getAllPlansOnce(): List<WorkoutPlan> = withContext(Dispatchers.IO) {
        planDao.getAllPlansOnce().ifEmpty { DefaultWorkoutData.defaultPlans }
    }
    fun getPlanForDay(dayOfWeek: Int): Flow<WorkoutPlan?> = planDao.getPlanForDay(dayOfWeek)
    suspend fun getPlanForDayOnce(dayOfWeek: Int): WorkoutPlan? = withContext(Dispatchers.IO) {
        planDao.getPlanForDayOnce(dayOfWeek)
    }
    suspend fun updatePlan(plan: WorkoutPlan) = withContext(Dispatchers.IO) {
        planDao.updatePlan(plan)
    }

    // Exercises
    fun getAllExercises(): Flow<List<ExerciseDefinition>> = exerciseDao.getAllExercises()
    fun getExercisesForDay(dayOfWeek: Int): Flow<List<ExerciseDefinition>> = exerciseDao.getExercisesForDay(dayOfWeek)
    suspend fun getExercisesForDayOnce(dayOfWeek: Int): List<ExerciseDefinition> = withContext(Dispatchers.IO) {
        exerciseDao.getExercisesForDayOnce(dayOfWeek)
    }
    suspend fun getExerciseById(id: String): ExerciseDefinition? = withContext(Dispatchers.IO) {
        exerciseDao.getExerciseById(id)
    }
    suspend fun updateExercise(exercise: ExerciseDefinition) = withContext(Dispatchers.IO) {
        exerciseDao.updateExercise(exercise)
    }

    // Workout sessions & logs
    fun getAllSessions(): Flow<List<WorkoutSessionLog>> = workoutLogDao.getAllSessions()
    fun getLastCompletedSession(): Flow<WorkoutSessionLog?> = workoutLogDao.getLastCompletedSession()
    suspend fun getSessionById(sessionId: Long): WorkoutSessionLog? = withContext(Dispatchers.IO) {
        workoutLogDao.getSessionById(sessionId)
    }
    suspend fun saveSession(session: WorkoutSessionLog): Long = withContext(Dispatchers.IO) {
        workoutLogDao.insertSession(session)
    }
    suspend fun updateSession(session: WorkoutSessionLog) = withContext(Dispatchers.IO) {
        workoutLogDao.updateSession(session)
    }
    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        workoutLogDao.deleteSetsForSession(sessionId)
        workoutLogDao.deleteSessionById(sessionId)
    }

    // Sets
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetLog>> = workoutLogDao.getSetsForSession(sessionId)
    suspend fun getSetsForSessionOnce(sessionId: Long): List<WorkoutSetLog> = withContext(Dispatchers.IO) {
        workoutLogDao.getSetsForSessionOnce(sessionId)
    }
    suspend fun saveSetLog(setLog: WorkoutSetLog): Long = withContext(Dispatchers.IO) {
        workoutLogDao.insertSetLog(setLog)
    }
    fun getLogsForExercise(exerciseId: String): Flow<List<WorkoutSetLog>> = workoutLogDao.getLogsForExercise(exerciseId)
    suspend fun getRecentLogsForExercise(exerciseId: String, limit: Int = 10): List<WorkoutSetLog> = withContext(Dispatchers.IO) {
        workoutLogDao.getRecentLogsForExercise(exerciseId, limit)
    }
    suspend fun getPersonalRecordReps(exerciseId: String): Int? = withContext(Dispatchers.IO) {
        workoutLogDao.getPersonalRecordReps(exerciseId)
    }

    // Weight Logs
    fun getAllWeightLogs(): Flow<List<WeightLog>> = weightDao.getAllWeightLogs()
    fun getLatestWeightLog(): Flow<WeightLog?> = weightDao.getLatestWeightLog()
    suspend fun addWeightLog(weightKg: Float, dateMillis: Long, notes: String = "") = withContext(Dispatchers.IO) {
        weightDao.insertWeightLog(WeightLog(weightKg = weightKg, dateEpochMillis = dateMillis, notes = notes))
        // also update user profile current weight
        val profile = profileDao.getUserProfileOnce() ?: DefaultWorkoutData.defaultProfile
        profileDao.insertOrUpdate(profile.copy(currentWeightKg = weightKg, updatedAt = System.currentTimeMillis()))
    }
    suspend fun deleteWeightLog(id: Long) = withContext(Dispatchers.IO) {
        weightDao.deleteWeightLogById(id)
    }

    // Food Logs
    fun getAllFoodLogs(): Flow<List<FoodLog>> = foodDao.getAllFoodLogs()
    suspend fun addFoodLog(log: FoodLog) = withContext(Dispatchers.IO) {
        foodDao.insertFoodLog(log)
    }
    suspend fun deleteFoodLog(id: Long) = withContext(Dispatchers.IO) {
        foodDao.deleteFoodLogById(id)
    }

    // Streak & Metrics calculation
    suspend fun calculateMetrics(): WorkoutMetrics = withContext(Dispatchers.IO) {
        val sessions = workoutLogDao.getAllSessionsOnce().filter { it.isCompleted }
        val totalCompleted = sessions.size

        // Streak calculation based on calendar days or weekly target
        var streakDays = 0
        if (sessions.isNotEmpty()) {
            val sessionDates = sessions.map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            }.toSet()

            val currentCal = Calendar.getInstance()
            val todayStr = "${currentCal.get(Calendar.YEAR)}-${currentCal.get(Calendar.DAY_OF_YEAR)}"
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStr = "${yesterdayCal.get(Calendar.YEAR)}-${yesterdayCal.get(Calendar.DAY_OF_YEAR)}"

            var checkCal = if (sessionDates.contains(todayStr)) {
                Calendar.getInstance()
            } else if (sessionDates.contains(yesterdayStr)) {
                yesterdayCal
            } else {
                null
            }

            if (checkCal != null) {
                while (true) {
                    val key = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.DAY_OF_YEAR)}"
                    if (sessionDates.contains(key)) {
                        streakDays++
                        checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            }
        }

        // Current week workouts
        val currentCal = Calendar.getInstance()
        val currentWeek = currentCal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = currentCal.get(Calendar.YEAR)
        val thisWeekCompleted = sessions.count {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
            cal.get(Calendar.WEEK_OF_YEAR) == currentWeek && cal.get(Calendar.YEAR) == currentYear
        }

        WorkoutMetrics(
            totalWorkoutsCompleted = totalCompleted,
            currentStreakDays = streakDays,
            thisWeekCompletedCount = thisWeekCompleted,
            weeklyTargetDays = 4
        )
    }

    // Export Data to JSON
    suspend fun exportDataToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val profile = profileDao.getUserProfileOnce() ?: DefaultWorkoutData.defaultProfile
        val profileObj = JSONObject().apply {
            put("name", profile.name)
            put("age", profile.age)
            put("heightCm", profile.heightCm.toDouble())
            put("currentWeightKg", profile.currentWeightKg.toDouble())
            put("targetWeightKg", profile.targetWeightKg.toDouble())
            put("experience", profile.experience)
            put("equipment", profile.equipment)
            put("daysPerWeek", profile.daysPerWeek)
            put("primaryGoal", profile.primaryGoal)
            put("updatedAt", profile.updatedAt)
        }
        root.put("profile", profileObj)

        // Weight logs
        val weights = weightDao.getAllWeightLogsOnce()
        val weightArr = JSONArray()
        weights.forEach {
            weightArr.put(JSONObject().apply {
                put("id", it.id)
                put("weightKg", it.weightKg.toDouble())
                put("dateEpochMillis", it.dateEpochMillis)
                put("notes", it.notes)
            })
        }
        root.put("weights", weightArr)

        // Workout Sessions
        val sessions = workoutLogDao.getAllSessionsOnce()
        val sessionArr = JSONArray()
        for (s in sessions) {
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("planName", s.planName)
                put("dayOfWeek", s.dayOfWeek)
                put("dateEpochMillis", s.dateEpochMillis)
                put("durationSeconds", s.durationSeconds)
                put("plannedExercisesCount", s.plannedExercisesCount)
                put("completedExercisesCount", s.completedExercisesCount)
                put("totalSetsCompleted", s.totalSetsCompleted)
                put("plannedRepsTotal", s.plannedRepsTotal)
                put("actualRepsTotal", s.actualRepsTotal)
                put("isCompleted", s.isCompleted)
                put("skippedExercisesList", s.skippedExercisesList)
                put("notes", s.notes)
            }
            val sets = workoutLogDao.getSetsForSessionOnce(s.id)
            val setArr = JSONArray()
            sets.forEach { setItem ->
                setArr.put(JSONObject().apply {
                    put("exerciseId", setItem.exerciseId)
                    put("exerciseName", setItem.exerciseName)
                    put("setNumber", setItem.setNumber)
                    put("targetReps", setItem.targetReps)
                    put("actualReps", setItem.actualReps)
                    if (setItem.actualRepsLeft != null) put("actualRepsLeft", setItem.actualRepsLeft)
                    if (setItem.actualRepsRight != null) put("actualRepsRight", setItem.actualRepsRight)
                    put("difficulty", setItem.difficulty)
                    put("isSkipped", setItem.isSkipped)
                    put("timestamp", setItem.timestamp)
                })
            }
            sObj.put("sets", setArr)
            sessionArr.put(sObj)
        }
        root.put("sessions", sessionArr)

        // Food logs
        val foods = foodDao.getAllFoodLogsOnce()
        val foodArr = JSONArray()
        foods.forEach {
            foodArr.put(JSONObject().apply {
                put("mealType", it.mealType)
                put("foodItem", it.foodItem)
                put("quantity", it.quantity)
                if (it.calories != null) put("calories", it.calories)
                if (it.proteinGrams != null) put("proteinGrams", it.proteinGrams.toDouble())
                put("notes", it.notes)
                put("dateEpochMillis", it.dateEpochMillis)
            })
        }
        root.put("foodLogs", foodArr)

        root.toString(2)
    }

    // Import Data from JSON
    suspend fun importDataFromJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                val importedProfile = UserProfile(
                    id = 1,
                    name = p.optString("name", ""),
                    age = p.optInt("age", 19),
                    heightCm = p.optDouble("heightCm", 182.0).toFloat(),
                    currentWeightKg = p.optDouble("currentWeightKg", 45.0).toFloat(),
                    targetWeightKg = p.optDouble("targetWeightKg", 65.0).toFloat(),
                    experience = p.optString("experience", "Some exercise experience, new to structured sets"),
                    equipment = p.optString("equipment", "Pull-up bar, chair, bodyweight, school bag"),
                    daysPerWeek = p.optInt("daysPerWeek", 4),
                    primaryGoal = p.optString("primaryGoal", "Gain weight and muscle at home without a gym"),
                    updatedAt = p.optLong("updatedAt", System.currentTimeMillis())
                )
                profileDao.insertOrUpdate(importedProfile)
            }

            if (root.has("weights")) {
                val wArr = root.getJSONArray("weights")
                for (i in 0 until wArr.length()) {
                    val wObj = wArr.getJSONObject(i)
                    weightDao.insertWeightLog(
                        WeightLog(
                            weightKg = wObj.getDouble("weightKg").toFloat(),
                            dateEpochMillis = wObj.optLong("dateEpochMillis", System.currentTimeMillis()),
                            notes = wObj.optString("notes", "")
                        )
                    )
                }
            }

            if (root.has("sessions")) {
                val sArr = root.getJSONArray("sessions")
                for (i in 0 until sArr.length()) {
                    val sObj = sArr.getJSONObject(i)
                    val sessionId = workoutLogDao.insertSession(
                        WorkoutSessionLog(
                            planName = sObj.optString("planName", "Workout"),
                            dayOfWeek = sObj.optInt("dayOfWeek", 1),
                            dateEpochMillis = sObj.optLong("dateEpochMillis", System.currentTimeMillis()),
                            durationSeconds = sObj.optInt("durationSeconds", 0),
                            plannedExercisesCount = sObj.optInt("plannedExercisesCount", 0),
                            completedExercisesCount = sObj.optInt("completedExercisesCount", 0),
                            totalSetsCompleted = sObj.optInt("totalSetsCompleted", 0),
                            plannedRepsTotal = sObj.optInt("plannedRepsTotal", 0),
                            actualRepsTotal = sObj.optInt("actualRepsTotal", 0),
                            isCompleted = sObj.optBoolean("isCompleted", true),
                            skippedExercisesList = sObj.optString("skippedExercisesList", ""),
                            notes = sObj.optString("notes", "")
                        )
                    )
                    if (sObj.has("sets")) {
                        val setArr = sObj.getJSONArray("sets")
                        for (j in 0 until setArr.length()) {
                            val setObj = setArr.getJSONObject(j)
                            workoutLogDao.insertSetLog(
                                WorkoutSetLog(
                                    sessionId = sessionId,
                                    exerciseId = setObj.optString("exerciseId", ""),
                                    exerciseName = setObj.optString("exerciseName", ""),
                                    setNumber = setObj.optInt("setNumber", 1),
                                    targetReps = setObj.optInt("targetReps", 10),
                                    actualReps = setObj.optInt("actualReps", 10),
                                    actualRepsLeft = if (setObj.has("actualRepsLeft")) setObj.getInt("actualRepsLeft") else null,
                                    actualRepsRight = if (setObj.has("actualRepsRight")) setObj.getInt("actualRepsRight") else null,
                                    difficulty = setObj.optString("difficulty", "Moderate"),
                                    isSkipped = setObj.optBoolean("isSkipped", false),
                                    timestamp = setObj.optLong("timestamp", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                }
            }

            if (root.has("foodLogs")) {
                val fArr = root.getJSONArray("foodLogs")
                for (i in 0 until fArr.length()) {
                    val fObj = fArr.getJSONObject(i)
                    foodDao.insertFoodLog(
                        FoodLog(
                            mealType = fObj.optString("mealType", "Meal"),
                            foodItem = fObj.optString("foodItem", ""),
                            quantity = fObj.optString("quantity", ""),
                            calories = if (fObj.has("calories")) fObj.getInt("calories") else null,
                            proteinGrams = if (fObj.has("proteinGrams")) fObj.getDouble("proteinGrams").toFloat() else null,
                            notes = fObj.optString("notes", ""),
                            dateEpochMillis = fObj.optLong("dateEpochMillis", System.currentTimeMillis())
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Clear all data
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        populateInitialData(database)
    }
}

data class WorkoutMetrics(
    val totalWorkoutsCompleted: Int = 0,
    val currentStreakDays: Int = 0,
    val thisWeekCompletedCount: Int = 0,
    val weeklyTargetDays: Int = 4
)
