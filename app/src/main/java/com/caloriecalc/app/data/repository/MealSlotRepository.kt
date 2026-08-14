package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.MealSlotDao
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.seed.MealSlotSeedData
import kotlinx.coroutines.flow.Flow

class MealSlotRepository(
    private val dao: MealSlotDao
) {
    suspend fun ensureSeeded() {
        if (dao.count() == 0) {
            dao.insertAll(MealSlotSeedData.defaults)
        }
    }

    fun observeActive(): Flow<List<MealSlot>> = dao.observeActive()

    fun observeAll(): Flow<List<MealSlot>> = dao.observeAll()

    suspend fun getById(id: Long): MealSlot? = dao.getById(id)

    suspend fun addMeal(name: String) {
        val nextOrder = dao.getMaxSortOrder() + 1
        dao.insert(MealSlot(name = name, sortOrder = nextOrder))
    }

    /** Archives rather than deletes, so historical entries logged under this meal stay intact. */
    suspend fun removeMeal(id: Long) {
        // Never let the active list drop to zero — the food-logging flow always needs somewhere to log to.
        if (dao.countActive() > 1) {
            dao.archive(id)
        }
    }

    suspend fun restoreMeal(id: Long) = dao.unarchive(id)
}
