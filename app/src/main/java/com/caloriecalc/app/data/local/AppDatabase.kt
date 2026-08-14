package com.caloriecalc.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.caloriecalc.app.data.local.dao.ExerciseDao
import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.dao.MealEntryDao
import com.caloriecalc.app.data.local.dao.MealSlotDao
import com.caloriecalc.app.data.local.dao.SetEntryDao
import com.caloriecalc.app.data.local.dao.UserProfileDao
import com.caloriecalc.app.data.local.dao.WeightLogDao
import com.caloriecalc.app.data.local.dao.WorkoutSessionDao
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.MealEntry
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.local.entity.SetEntry
import com.caloriecalc.app.data.local.entity.UserProfile
import com.caloriecalc.app.data.local.entity.WeightLog
import com.caloriecalc.app.data.local.entity.WorkoutSession

@Database(
    entities = [
        FoodItem::class,
        MealEntry::class,
        MealSlot::class,
        UserProfile::class,
        WeightLog::class,
        Exercise::class,
        WorkoutSession::class,
        SetEntry::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun mealSlotDao(): MealSlotDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun setEntryDao(): SetEntryDao

    companion object {
        const val DATABASE_NAME = "caloriecalc.db"
    }
}
