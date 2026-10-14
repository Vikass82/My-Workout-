package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExerciseDefinition
import com.example.data.model.FoodLog
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutSessionLog
import com.example.data.model.WorkoutSetLog
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)
}

@Dao
interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plans ORDER BY dayOfWeek ASC")
    fun getAllPlans(): Flow<List<WorkoutPlan>>

    @Query("SELECT * FROM workout_plans ORDER BY dayOfWeek ASC")
    suspend fun getAllPlansOnce(): List<WorkoutPlan>

    @Query("SELECT * FROM workout_plans WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    fun getPlanForDay(dayOfWeek: Int): Flow<WorkoutPlan?>

    @Query("SELECT * FROM workout_plans WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getPlanForDayOnce(dayOfWeek: Int): WorkoutPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<WorkoutPlan>)

    @Update
    suspend fun updatePlan(plan: WorkoutPlan)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_definitions ORDER BY dayOfWeek ASC, orderIndex ASC")
    fun getAllExercises(): Flow<List<ExerciseDefinition>>

    @Query("SELECT * FROM exercise_definitions ORDER BY dayOfWeek ASC, orderIndex ASC")
    suspend fun getAllExercisesOnce(): List<ExerciseDefinition>

    @Query("SELECT * FROM exercise_definitions WHERE dayOfWeek = :dayOfWeek ORDER BY orderIndex ASC")
    fun getExercisesForDay(dayOfWeek: Int): Flow<List<ExerciseDefinition>>

    @Query("SELECT * FROM exercise_definitions WHERE dayOfWeek = :dayOfWeek ORDER BY orderIndex ASC")
    suspend fun getExercisesForDayOnce(dayOfWeek: Int): List<ExerciseDefinition>

    @Query("SELECT * FROM exercise_definitions WHERE id = :exerciseId LIMIT 1")
    suspend fun getExerciseById(exerciseId: String): ExerciseDefinition?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseDefinition>)

    @Update
    suspend fun updateExercise(exercise: ExerciseDefinition)

    @Query("DELETE FROM exercise_definitions WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: String)
}

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_session_logs ORDER BY dateEpochMillis DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionLog>>

    @Query("SELECT * FROM workout_session_logs ORDER BY dateEpochMillis DESC")
    suspend fun getAllSessionsOnce(): List<WorkoutSessionLog>

    @Query("SELECT * FROM workout_session_logs WHERE isCompleted = 1 ORDER BY dateEpochMillis DESC LIMIT 1")
    fun getLastCompletedSession(): Flow<WorkoutSessionLog?>

    @Query("SELECT * FROM workout_session_logs WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionLog): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionLog)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionLog)

    @Query("DELETE FROM workout_session_logs WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    // Set logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(setLog: WorkoutSetLog): Long

    @Query("SELECT * FROM workout_set_logs WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetLog>>

    @Query("SELECT * FROM workout_set_logs WHERE sessionId = :sessionId ORDER BY id ASC")
    suspend fun getSetsForSessionOnce(sessionId: Long): List<WorkoutSetLog>

    @Query("SELECT * FROM workout_set_logs WHERE exerciseId = :exerciseId AND isSkipped = 0 ORDER BY timestamp DESC")
    fun getLogsForExercise(exerciseId: String): Flow<List<WorkoutSetLog>>

    @Query("SELECT * FROM workout_set_logs WHERE exerciseId = :exerciseId AND isSkipped = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogsForExercise(exerciseId: String, limit: Int = 10): List<WorkoutSetLog>

    @Query("SELECT MAX(actualReps) FROM workout_set_logs WHERE exerciseId = :exerciseId AND isSkipped = 0")
    suspend fun getPersonalRecordReps(exerciseId: String): Int?

    @Query("DELETE FROM workout_set_logs WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Long)
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY dateEpochMillis ASC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs ORDER BY dateEpochMillis ASC")
    suspend fun getAllWeightLogsOnce(): List<WeightLog>

    @Query("SELECT * FROM weight_logs ORDER BY dateEpochMillis DESC LIMIT 1")
    fun getLatestWeightLog(): Flow<WeightLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog): Long

    @Delete
    suspend fun deleteWeightLog(weightLog: WeightLog)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Long)
}

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs ORDER BY dateEpochMillis DESC")
    fun getAllFoodLogs(): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs ORDER BY dateEpochMillis DESC")
    suspend fun getAllFoodLogsOnce(): List<FoodLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog): Long

    @Delete
    suspend fun deleteFoodLog(foodLog: FoodLog)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLogById(id: Long)
}
