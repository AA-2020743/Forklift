package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.dao.MealEntryDao
import com.caloriecalc.app.data.local.entity.withAddedSugarAssumedFromTotal
import com.caloriecalc.app.domain.MicronutrientEstimator

/**
 * One-shot repair for foods saved before micronutrient auto-fill existed.
 *
 * Anything entered by hand previously stored no micronutrients at all, which is why the
 * Micronutrients screen read 0.0 for every row regardless of what had been eaten. Filling only
 * *new* foods would leave existing history permanently blank, so this walks the saved foods,
 * attaches a reference profile wherever one can be matched by name and nothing is recorded yet,
 * then re-derives the snapshot on every already-logged entry of those foods so past days show
 * real numbers too.
 *
 * Deliberately conservative: a food that already has any micronutrient value is left untouched,
 * and a name that matches no reference profile stays empty rather than being given invented data.
 */
class MicronutrientBackfill(
    private val foodDao: FoodDao,
    private val mealEntryDao: MealEntryDao
) {
    /** Re-derives every logged micronutrient snapshot after a food is edited. */
    suspend fun resyncEntriesForFood(foodId: Long) {
        val food = foodDao.getById(foodId) ?: return
        if (MicronutrientEstimator.isEmpty(food.micronutrients)) return
        mealEntryDao.getEntriesForFood(foodId).forEach { entry ->
            val snapshot = food.micronutrients.scaledBy(entry.grams / 100.0)
            mealEntryDao.update(entry.copy(micronutrients = snapshot))
        }
    }

    suspend fun run(): Int {
        var repaired = 0
        foodDao.getAll().forEach { food ->
            if (!MicronutrientEstimator.isEmpty(food.micronutrients)) return@forEach
            val guess = MicronutrientEstimator.guessPer100g(food.name)
                ?.withAddedSugarAssumedFromTotal()
                ?: return@forEach

            foodDao.update(food.copy(micronutrients = guess))
            mealEntryDao.getEntriesForFood(food.id).forEach { entry ->
                mealEntryDao.update(entry.copy(micronutrients = guess.scaledBy(entry.grams / 100.0)))
            }
            repaired++
        }
        return repaired
    }
}
