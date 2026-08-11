package com.caloriecalc.app.di

import android.content.Context
import androidx.room.Room
import com.caloriecalc.app.data.local.AppDatabase
import com.caloriecalc.app.data.remote.NetworkClient
import com.caloriecalc.app.data.repository.FoodRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.data.repository.WeightRepository
import com.caloriecalc.app.data.repository.WorkoutRepository
import com.caloriecalc.app.reminder.ReminderScheduler

/**
 * Simple hand-rolled dependency container (no DI framework) — appropriate for a
 * single-module app of this size and keeps the dependency graph easy to reason about
 * without an annotation-processing step.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).build()

    private val openFoodFactsApi = NetworkClient.createOpenFoodFactsApi()

    val foodRepository: FoodRepository by lazy {
        FoodRepository(database.foodDao(), openFoodFactsApi)
    }

    val nutritionLogRepository: NutritionLogRepository by lazy {
        NutritionLogRepository(database.mealEntryDao(), database.foodDao())
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.userProfileDao())
    }

    val weightRepository: WeightRepository by lazy {
        WeightRepository(database.weightLogDao())
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(database.exerciseDao(), database.workoutSessionDao(), database.setEntryDao())
    }

    val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(context.applicationContext)
    }
}
