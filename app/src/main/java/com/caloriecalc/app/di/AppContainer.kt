package com.caloriecalc.app.di

import android.content.Context
import androidx.room.Room
import com.caloriecalc.app.data.local.ALL_MIGRATIONS
import com.caloriecalc.app.data.local.ApiKeyStore
import com.caloriecalc.app.data.local.AppDatabase
import com.caloriecalc.app.data.remote.GeminiClient
import com.caloriecalc.app.data.remote.NetworkClient
import com.caloriecalc.app.data.repository.BodyMeasurementRepository
import com.caloriecalc.app.data.repository.FoodRepository
import com.caloriecalc.app.data.repository.MealSlotRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.PhotoEstimationRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.data.repository.WeightRepository
import com.caloriecalc.app.data.repository.WorkoutRepository
import com.caloriecalc.app.data.repository.WorkoutTemplateRepository
import com.caloriecalc.app.reminder.ReminderScheduler

/**
 * Simple hand-rolled dependency container (no DI framework) — appropriate for a
 * single-module app of this size and keeps the dependency graph easy to reason about
 * without an annotation-processing step.
 */
class AppContainer(context: Context) {

    // Real migrations (Migrations.kt) preserve on-device data (logged foods, workouts, weight
    // history, etc.) across schema changes. Every future entity change must ship a matching
    // Migration in ALL_MIGRATIONS — destructive fallback only covers a downgrade (installing
    // an older build over a newer DB), which can't reasonably be migrated forward anyway.
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).addMigrations(*ALL_MIGRATIONS)
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    private val openFoodFactsApi = NetworkClient.createOpenFoodFactsApi()
    private val geminiApi = GeminiClient.createApi()

    val apiKeyStore: ApiKeyStore by lazy { ApiKeyStore(context.applicationContext) }

    val foodRepository: FoodRepository by lazy {
        FoodRepository(database.foodDao(), openFoodFactsApi)
    }

    val nutritionLogRepository: NutritionLogRepository by lazy {
        NutritionLogRepository(database.mealEntryDao(), database.foodDao())
    }

    val mealSlotRepository: MealSlotRepository by lazy {
        MealSlotRepository(database.mealSlotDao())
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.userProfileDao())
    }

    val weightRepository: WeightRepository by lazy {
        WeightRepository(database.weightLogDao())
    }

    val bodyMeasurementRepository: BodyMeasurementRepository by lazy {
        BodyMeasurementRepository(database.bodyMeasurementDao())
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(database.exerciseDao(), database.workoutSessionDao(), database.setEntryDao())
    }

    val workoutTemplateRepository: WorkoutTemplateRepository by lazy {
        WorkoutTemplateRepository(database.workoutTemplateDao(), workoutRepository)
    }

    val photoEstimationRepository: PhotoEstimationRepository by lazy {
        PhotoEstimationRepository(geminiApi, apiKeyStore)
    }

    val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(context.applicationContext)
    }
}
