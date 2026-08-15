package com.caloriecalc.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.caloriecalc.app.data.local.dao.ActivityLogDao
import com.caloriecalc.app.data.local.dao.BodyMeasurementDao
import com.caloriecalc.app.data.local.dao.ExerciseDao
import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.dao.MealEntryDao
import com.caloriecalc.app.data.local.dao.MealSlotDao
import com.caloriecalc.app.data.local.dao.SetEntryDao
import com.caloriecalc.app.data.local.dao.UserProfileDao
import com.caloriecalc.app.data.local.dao.WaterLogDao
import com.caloriecalc.app.data.local.dao.WeightLogDao
import com.caloriecalc.app.data.local.dao.WorkoutSessionDao
import com.caloriecalc.app.data.local.dao.WorkoutTemplateDao
import com.caloriecalc.app.data.local.entity.ActivityLog
import com.caloriecalc.app.data.local.entity.BodyMeasurement
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.MealEntry
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.local.entity.SetEntry
import com.caloriecalc.app.data.local.entity.UserProfile
import com.caloriecalc.app.data.local.entity.WaterLog
import com.caloriecalc.app.data.local.entity.WeightLog
import com.caloriecalc.app.data.local.entity.WorkoutSession
import com.caloriecalc.app.data.local.entity.WorkoutTemplate
import com.caloriecalc.app.data.local.entity.WorkoutTemplateExercise

@Database(
    entities = [
        FoodItem::class,
        MealEntry::class,
        MealSlot::class,
        UserProfile::class,
        WeightLog::class,
        BodyMeasurement::class,
        Exercise::class,
        WorkoutSession::class,
        SetEntry::class,
        WorkoutTemplate::class,
        WorkoutTemplateExercise::class,
        ActivityLog::class,
        WaterLog::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun mealSlotDao(): MealSlotDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun setEntryDao(): SetEntryDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun waterLogDao(): WaterLogDao

    companion object {
        const val DATABASE_NAME = "caloriecalc.db"
    }
}
