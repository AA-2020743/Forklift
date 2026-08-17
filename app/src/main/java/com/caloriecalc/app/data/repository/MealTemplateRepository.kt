package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.dao.MealEntryDao
import com.caloriecalc.app.data.local.dao.MealTemplateDao
import com.caloriecalc.app.data.local.dao.MealSlotDao
import com.caloriecalc.app.data.local.dao.MealTimeDao
import com.caloriecalc.app.data.local.AppDatabase
import com.caloriecalc.app.data.local.dao.MealTemplateSummary
import com.caloriecalc.app.data.local.entity.MealEntry
import com.caloriecalc.app.data.local.entity.MealTemplate
import com.caloriecalc.app.data.local.entity.MealTemplateItem
import com.caloriecalc.app.domain.HydrationCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import androidx.room.withTransaction

class MealTemplateRepository(
    private val database: AppDatabase,
    private val templateDao: MealTemplateDao,
    private val mealEntryDao: MealEntryDao,
    private val foodDao: FoodDao,
    private val mealTimeDao: MealTimeDao,
    private val mealSlotDao: MealSlotDao
) {
    fun observeSummaries(): Flow<List<MealTemplateSummary>> = templateDao.observeSummaries()

    /** Snapshots a currently-logged meal's foods+grams as a new reusable template. */
    suspend fun saveFromMeal(name: String, epochDay: Long, mealSlotId: Long) {
        database.withTransaction {
            val entries = mealEntryDao.getEntriesForMeal(epochDay, mealSlotId).first()
            val templateId = templateDao.insertTemplate(MealTemplate(name = name.trim()))
            templateDao.insertItems(
                entries.map { entry -> MealTemplateItem(templateId = templateId, foodItemId = entry.foodItemId, grams = entry.grams) }
            )
        }
    }

    /** Logs every ingredient of a template into a meal, recomputing macros from each food's
     * current per-100g values (not a frozen snapshot from when the template was saved). */
    suspend fun applyTemplate(templateId: Long, mealSlotId: Long, epochDay: Long) {
        database.withTransaction {
            val loggedAt = resolveMealTime(
                mealTimeDao = mealTimeDao,
                mealSlotDao = mealSlotDao,
                epochDay = epochDay,
                mealSlotId = mealSlotId,
                requestedEpochMillis = null,
                forceRequested = false
            )
            mealEntryDao.updateTimeForMeal(epochDay, mealSlotId, loggedAt)
            templateDao.getItems(templateId).forEach { item ->
                val food = foodDao.getById(item.foodItemId) ?: return@forEach
                val factor = item.grams / 100.0
                mealEntryDao.insert(
                    MealEntry(
                        foodItemId = food.id,
                        mealSlotId = mealSlotId,
                        epochDay = epochDay,
                        loggedAtEpochMillis = loggedAt,
                        grams = item.grams,
                        calories = food.caloriesPer100g * factor,
                        protein = food.proteinPer100g * factor,
                        fat = food.fatPer100g * factor,
                        carbs = food.carbsPer100g * factor,
                        micronutrients = food.micronutrients.scaledBy(factor),
                        hydrationMl = HydrationCalculator.hydrationMl(item.grams, food.waterContentPercent, food.name)
                    )
                )
                foodDao.markUsed(food.id, loggedAt)
            }
        }
    }

    suspend fun deleteTemplate(id: Long) = templateDao.deleteTemplate(id)
}
