package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.WeightLogDao
import com.caloriecalc.app.data.local.entity.WeightLog
import kotlinx.coroutines.flow.Flow

class WeightRepository(private val dao: WeightLogDao) {

    fun observeAll(): Flow<List<WeightLog>> = dao.observeAll()

    fun observeInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<WeightLog>> =
        dao.observeInRange(fromEpochDay, toEpochDay)

    fun observeLatest(): Flow<WeightLog?> = dao.observeLatest()

    suspend fun hasLoggedForDay(epochDay: Long): Boolean = dao.getForDay(epochDay) != null

    suspend fun logWeight(epochDay: Long, weightKg: Double) {
        require(weightKg.isFinite() && weightKg > 0.0)
        dao.upsert(WeightLog(epochDay = epochDay, weightKg = weightKg))
    }

    suspend fun deleteForDay(epochDay: Long) = dao.deleteForDay(epochDay)
}
