package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.MealSlotDao
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.seed.MealSlotSeedData
import java.time.LocalTime
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

    /**
     * New meals default to the current wall-clock time — you generally add "Second lunch" while
     * you're eating it — which is immediately adjustable and gives protein-spacing reminders
     * something to anchor on without an extra setup step.
     */
    suspend fun addMeal(name: String, now: LocalTime = LocalTime.now()) {
        require(name.isNotBlank())
        val nextOrder = dao.getMaxSortOrder() + 1
        dao.insert(
            MealSlot(
                name = name,
                sortOrder = nextOrder,
                targetHour = now.hour,
                targetMinute = now.minute
            )
        )
    }

    suspend fun setMealTime(id: Long, hour: Int?, minute: Int?) {
        require((hour == null) == (minute == null))
        require(hour == null || hour in 0..23)
        require(minute == null || minute in 0..59)
        dao.updateTargetTime(id, hour, minute)
    }

    suspend fun setRemindersEnabled(id: Long, enabled: Boolean) = dao.updateRemindersEnabled(id, enabled)

    /** Archives rather than deletes, so historical entries logged under this meal stay intact. */
    suspend fun removeMeal(id: Long) {
        // Never let the active list drop to zero — the food-logging flow always needs somewhere to log to.
        if (dao.countActive() > 1) {
            dao.archive(id)
        }
    }

    suspend fun restoreMeal(id: Long) = dao.unarchive(id)
}
