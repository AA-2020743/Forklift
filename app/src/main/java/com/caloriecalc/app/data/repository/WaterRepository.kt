package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.WaterLogDao
import com.caloriecalc.app.data.local.entity.WaterLog
import kotlinx.coroutines.flow.Flow

class WaterRepository(private val dao: WaterLogDao) {

    fun observeForDay(epochDay: Long): Flow<WaterLog?> = dao.observeForDay(epochDay)

    fun observeInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<WaterLog>> =
        dao.observeInRange(fromEpochDay, toEpochDay)

    /** Adds (or, with a negative delta, removes) water from the running total for the day,
     * never letting it go below zero. */
    suspend fun addWater(epochDay: Long, deltaMl: Int) {
        val currentMl = dao.getForDay(epochDay)?.amountMl ?: 0
        dao.upsert(WaterLog(epochDay = epochDay, amountMl = (currentMl + deltaMl).coerceAtLeast(0)))
    }
}
