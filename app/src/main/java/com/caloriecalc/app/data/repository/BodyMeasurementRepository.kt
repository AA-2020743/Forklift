package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.BodyMeasurementDao
import com.caloriecalc.app.data.local.entity.BodyMeasurement
import kotlinx.coroutines.flow.Flow

class BodyMeasurementRepository(private val dao: BodyMeasurementDao) {

    fun observeAll(): Flow<List<BodyMeasurement>> = dao.observeAll()

    suspend fun getForDay(epochDay: Long): BodyMeasurement? = dao.getForDay(epochDay)

    suspend fun upsert(measurement: BodyMeasurement) {
        require(listOf(
            measurement.waistCm,
            measurement.chestCm,
            measurement.armsCm,
            measurement.thighsCm,
            measurement.hipsCm,
            measurement.neckCm
        ).all { it == null || (it.isFinite() && it > 0.0) })
        require(listOf(
            measurement.waistCm,
            measurement.chestCm,
            measurement.armsCm,
            measurement.thighsCm,
            measurement.hipsCm,
            measurement.neckCm
        ).any { it != null })
        dao.upsert(measurement)
    }

    suspend fun deleteForDay(epochDay: Long) = dao.deleteForDay(epochDay)
}
