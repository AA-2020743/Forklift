package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.DayMacroTotals
import com.caloriecalc.app.data.local.dao.DayMacroTotalsWithDay
import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.dao.MealEntryDao
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.MealEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class MealEntryWithFood(val entry: MealEntry, val food: FoodItem)

class NutritionLogRepository(
    private val mealEntryDao: MealEntryDao,
    private val foodDao: FoodDao
) {
    fun entriesForDay(epochDay: Long): Flow<List<MealEntryWithFood>> =
        mealEntryDao.getEntriesForDay(epochDay).map { it.attachFood() }

    fun entriesForMeal(epochDay: Long, mealSlotId: Long): Flow<List<MealEntryWithFood>> =
        mealEntryDao.getEntriesForMeal(epochDay, mealSlotId).map { it.attachFood() }

    fun totalsForDay(epochDay: Long): Flow<DayMacroTotals> = mealEntryDao.getTotalsForDay(epochDay)

    fun totalsInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DayMacroTotalsWithDay>> =
        mealEntryDao.getTotalsInRange(fromEpochDay, toEpochDay)

    suspend fun logFood(food: FoodItem, mealSlotId: Long, epochDay: Long, grams: Double) {
        val factor = grams / 100.0
        val loggedAt = System.currentTimeMillis()
        val entry = MealEntry(
            foodItemId = food.id,
            mealSlotId = mealSlotId,
            epochDay = epochDay,
            loggedAtEpochMillis = loggedAt,
            grams = grams,
            calories = food.caloriesPer100g * factor,
            protein = food.proteinPer100g * factor,
            fat = food.fatPer100g * factor,
            carbs = food.carbsPer100g * factor,
            micronutrients = food.micronutrients.scaledBy(factor)
        )
        mealEntryDao.insert(entry)
        foodDao.markUsed(food.id, loggedAt)
    }

    suspend fun deleteEntry(entry: MealEntry) = mealEntryDao.delete(entry)

    /** Copies every entry logged for a meal on one day onto another — the "repeat yesterday"
     * quick action. Each copy gets a fresh id and logged-at timestamp; the source day is untouched. */
    suspend fun repeatMeal(fromEpochDay: Long, toEpochDay: Long, mealSlotId: Long) {
        val sourceEntries = mealEntryDao.getEntriesForMeal(fromEpochDay, mealSlotId).first()
        val loggedAt = System.currentTimeMillis()
        sourceEntries.forEach { entry ->
            mealEntryDao.insert(entry.copy(id = 0, epochDay = toEpochDay, loggedAtEpochMillis = loggedAt))
            foodDao.markUsed(entry.foodItemId, loggedAt)
        }
    }

    /** Re-derives calories/macros/micronutrients for a logged entry from its food's per-100g
     * values at a new gram amount — used when correcting how much of something was actually eaten. */
    suspend fun updateEntryQuantity(entry: MealEntry, food: FoodItem, grams: Double) {
        val factor = grams / 100.0
        mealEntryDao.update(
            entry.copy(
                grams = grams,
                calories = food.caloriesPer100g * factor,
                protein = food.proteinPer100g * factor,
                fat = food.fatPer100g * factor,
                carbs = food.carbsPer100g * factor,
                micronutrients = food.micronutrients.scaledBy(factor)
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
