package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ExerciseDao
import com.example.data.dao.FoodLogDao
import com.example.data.dao.UserProfileDao
import com.example.data.dao.WeightLogDao
import com.example.data.dao.WorkoutLogDao
import com.example.data.dao.WorkoutPlanDao
import com.example.data.model.ExerciseDefinition
import com.example.data.model.FoodLog
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutSessionLog
import com.example.data.model.WorkoutSetLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        WorkoutPlan::class,
        ExerciseDefinition::class,
        WorkoutSessionLog::class,
        WorkoutSetLog::class,
        WeightLog::class,
        FoodLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_journey_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    // Populate user profile if empty
    if (database.userProfileDao().getUserProfileOnce() == null) {
        database.userProfileDao().insertOrUpdate(DefaultWorkoutData.defaultProfile)
    }
    // Populate workout plans if empty
    if (database.workoutPlanDao().getAllPlansOnce().isEmpty()) {
        database.workoutPlanDao().insertPlans(DefaultWorkoutData.defaultPlans)
    }
    // Populate exercises if empty, or insert any missing default exercises
    val existingExercises = database.exerciseDao().getAllExercisesOnce()
    if (existingExercises.isEmpty()) {
        database.exerciseDao().insertExercises(DefaultWorkoutData.defaultExercises)
    } else {
        val existingIds = existingExercises.map { it.id }.toSet()
        val missing = DefaultWorkoutData.defaultExercises.filter { !existingIds.contains(it.id) }
        if (missing.isNotEmpty()) {
            database.exerciseDao().insertExercises(missing)
        }
    }
    // Populate baseline weight log if empty
    if (database.weightLogDao().getAllWeightLogsOnce().isEmpty()) {
        database.weightLogDao().insertWeightLog(DefaultWorkoutData.defaultInitialWeight)
    }
}
