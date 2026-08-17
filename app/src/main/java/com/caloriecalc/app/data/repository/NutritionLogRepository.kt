package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.DayHydration
import com.caloriecalc.app.data.local.dao.DayMacroTotals
import com.caloriecalc.app.data.local.dao.DayMacroTotalsWithDay
import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.dao.MealEntryDao
import com.caloriecalc.app.data.local.dao.MealSlotDao
import com.caloriecalc.app.data.local.dao.MealTimeDao
import com.caloriecalc.app.data.local.AppDatabase
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.MealEntry
import com.caloriecalc.app.data.local.entity.MealTime
import com.caloriecalc.app.domain.HydrationCalculator
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class MealEntryWithFood(val entry: MealEntry, val food: FoodItem)

class NutritionLogRepository(
    private val database: AppDatabase,
    private val mealEntryDao: MealEntryDao,
    private val foodDao: FoodDao,
    private val mealTimeDao: MealTimeDao,
    private val mealSlotDao: MealSlotDao
) {
    fun entriesForDay(epochDay: Long): Flow<List<MealEntryWithFood>> =
        mealEntryDao.getEntriesForDay(epochDay).map { it.attachFood() }

    fun entriesForMeal(epochDay: Long, mealSlotId: Long): Flow<List<MealEntryWithFood>> =
        mealEntryDao.getEntriesForMeal(epochDay, mealSlotId).map { it.attachFood() }

    fun observeMealTime(epochDay: Long, mealSlotId: Long): Flow<Long?> =
        mealTimeDao.observe(epochDay, mealSlotId).map { it?.consumedAtEpochMillis }

    suspend fun getMealTime(epochDay: Long, mealSlotId: Long): Long? =
        mealTimeDao.get(epochDay, mealSlotId)?.consumedAtEpochMillis

    fun totalsForDay(epochDay: Long): Flow<DayMacroTotals> = mealEntryDao.getTotalsForDay(epochDay)

    fun totalsInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DayMacroTotalsWithDay>> =
        mealEntryDao.getTotalsInRange(fromEpochDay, toEpochDay)

    /** Hydration contributed by food and drink on a day, in mL (separate from logged plain water). */
    fun hydrationForDay(epochDay: Long): Flow<Double> = mealEntryDao.getHydrationForDay(epochDay)

    fun hydrationInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DayHydration>> =
        mealEntryDao.getHydrationInRange(fromEpochDay, toEpochDay)

    suspend fun lastProteinEntry(
        minProteinGrams: Double,
        notAfterEpochMillis: Long = System.currentTimeMillis()
    ): MealEntry? = mealEntryDao.getLastProteinEntry(minProteinGrams, notAfterEpochMillis)

    suspend fun logFood(
        food: FoodItem,
        mealSlotId: Long,
        epochDay: Long,
        grams: Double,
        consumedAtEpochMillis: Long = System.currentTimeMillis(),
        setMealTime: Boolean = false
    ) {
        require(grams.isFinite() && grams > 0.0)
        database.withTransaction {
            val mealTime = resolveMealTime(
                mealTimeDao = mealTimeDao,
                mealSlotDao = mealSlotDao,
                epochDay = epochDay,
                mealSlotId = mealSlotId,
                requestedEpochMillis = consumedAtEpochMillis,
                forceRequested = setMealTime
            )
            // Keep legacy entries and entries created by other logging paths aligned with the
            // shared meal time before inserting the next component.
            mealEntryDao.updateTimeForMeal(epochDay, mealSlotId, mealTime)
            val factor = grams / 100.0
            mealEntryDao.insert(
                MealEntry(
                    foodItemId = food.id,
                    mealSlotId = mealSlotId,
                    epochDay = epochDay,
                    loggedAtEpochMillis = mealTime,
                    grams = grams,
                    calories = food.caloriesPer100g * factor,
                    protein = food.proteinPer100g * factor,
                    fat = food.fatPer100g * factor,
                    carbs = food.carbsPer100g * factor,
                    micronutrients = food.micronutrients.scaledBy(factor),
                    hydrationMl = HydrationCalculator.hydrationMl(grams, food.waterContentPercent, food.name)
                )
            )
            foodDao.markUsed(food.id, mealTime)
        }
    }

    suspend fun deleteEntry(entry: MealEntry) = mealEntryDao.delete(entry)

    /** Sets one consumed-at time for the complete meal on the selected day. */
    suspend fun setMealTime(epochDay: Long, mealSlotId: Long, consumedAtEpochMillis: Long) {
        database.withTransaction {
            val safeTime = normalizeMealTimeForDay(epochDay, consumedAtEpochMillis)
            mealTimeDao.upsert(MealTime(epochDay, mealSlotId, safeTime))
            mealEntryDao.updateTimeForMeal(epochDay, mealSlotId, safeTime)
        }
    }

    /** Copies every entry logged for a meal on one day onto another — the "repeat yesterday"
     * quick action. Each copy gets a fresh id and logged-at timestamp; the source day is untouched. */
    suspend fun repeatMeal(fromEpochDay: Long, toEpochDay: Long, mealSlotId: Long) {
        database.withTransaction {
            val sourceEntries = mealEntryDao.getEntriesForMeal(fromEpochDay, mealSlotId).first()
            if (sourceEntries.isEmpty()) return@withTransaction
            val sourceTime = mealTimeDao.get(fromEpochDay, mealSlotId)?.consumedAtEpochMillis
                ?: sourceEntries.minOf { it.loggedAtEpochMillis }
            val loggedAt = moveMealTimeToDay(toEpochDay, sourceTime)
            mealTimeDao.upsert(MealTime(toEpochDay, mealSlotId, loggedAt))
            sourceEntries.forEach { entry ->
                mealEntryDao.insert(entry.copy(id = 0, epochDay = toEpochDay, loggedAtEpochMillis = loggedAt))
                foodDao.markUsed(entry.foodItemId, loggedAt)
            }
        }
    }

    /** Re-derives calories/macros/micronutrients for a logged entry from its food's per-100g
     * values at a new gram amount — used when correcting how much of something was actually eaten. */
    suspend fun updateEntryQuantity(entry: MealEntry, food: FoodItem, grams: Double) {
        require(grams.isFinite() && grams > 0.0)
        val factor = grams / 100.0
        mealEntryDao.update(
            entry.copy(
                grams = grams,
                calories = food.caloriesPer100g * factor,
                protein = food.proteinPer100g * factor,
                fat = food.fatPer100g * factor,
                carbs = food.carbsPer100g * factor,
                micronutrients = food.micronutrients.scaledBy(factor),
                hydrationMl = HydrationCalculator.hydrationMl(grams, food.waterContentPercent, food.name)
            )
        )
    }

    // `map` here is List's inline map; suspend calls inside it are fine because the
    // enclosing Flow.map transform (the caller of this function) is itself suspend.
    private suspend fun List<MealEntry>.attachFood(): List<MealEntryWithFood> =
        mapNotNull { entry -> foodDao.getById(entry.foodItemId)?.let { MealEntryWithFood(entry, it) } }
}

/** Converts a serving count into grams using the food's defined serving size, defaulting to 100g. */
fun FoodItem.gramsForServings(servings: Double): Double = servings * (servingSizeGrams ?: 100.0)
