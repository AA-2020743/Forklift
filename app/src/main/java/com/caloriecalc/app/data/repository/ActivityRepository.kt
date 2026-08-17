package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.ActivityLogDao
import com.caloriecalc.app.data.local.entity.ActivityLog
import com.caloriecalc.app.domain.ActivityCalculator
import com.caloriecalc.app.domain.ActivityType
import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val dao: ActivityLogDao) {

    fun observeForDay(epochDay: Long): Flow<List<ActivityLog>> = dao.observeForDay(epochDay)

    suspend fun logActivity(
        epochDay: Long,
        type: ActivityType,
        durationMinutes: Int,
        bodyWeightKg: Double,
        steps: Int? = null,
        caloriesBurnedOverride: Int? = null
    ): Long {
        require(durationMinutes > 0)
        require(bodyWeightKg.isFinite() && bodyWeightKg > 0.0)
        require(steps == null || steps >= 0)
        require(caloriesBurnedOverride == null || caloriesBurnedOverride >= 0)
        val calories = caloriesBurnedOverride
            ?: ActivityCalculator.estimateCaloriesBurned(type.met, durationMinutes, bodyWeightKg)
        return dao.insert(
            ActivityLog(
                epochDay = epochDay,
                type = type,
                durationMinutes = durationMinutes,
                steps = steps,
                caloriesBurned = calories
            )
        )
    }

    suspend fun deleteActivity(activity: ActivityLog) = dao.delete(activity)
}
