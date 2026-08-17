package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.MealSlotDao
import com.caloriecalc.app.data.local.dao.MealTimeDao
import com.caloriecalc.app.data.local.entity.MealTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Resolves one wall-clock time for a meal/day. All logging paths use this so a past-day entry
 * cannot accidentally receive the moment the user happened to open the app.
 */
internal suspend fun resolveMealTime(
    mealTimeDao: MealTimeDao,
    mealSlotDao: MealSlotDao,
    epochDay: Long,
    mealSlotId: Long,
    requestedEpochMillis: Long?,
    forceRequested: Boolean
): Long {
    val stored = mealTimeDao.get(epochDay, mealSlotId)
    if (!forceRequested && stored != null) {
        val normalized = normalizeMealTimeForDay(epochDay, stored.consumedAtEpochMillis)
        if (normalized != stored.consumedAtEpochMillis) {
            mealTimeDao.upsert(stored.copy(consumedAtEpochMillis = normalized))
        }
        return normalized
    }

    val fallback = stored?.consumedAtEpochMillis
        ?: defaultMealTime(epochDay, mealSlotDao.getById(mealSlotId))
    val candidate = if (forceRequested) requestedEpochMillis ?: fallback else fallback
    val resolved = normalizeMealTimeForDay(epochDay, candidate)
    if (forceRequested || stored == null || stored.consumedAtEpochMillis != resolved) {
        mealTimeDao.upsert(
            MealTime(
                epochDay = epochDay,
                mealSlotId = mealSlotId,
                consumedAtEpochMillis = resolved
            )
        )
    }
    return resolved
}

internal fun defaultMealTime(epochDay: Long, slot: com.caloriecalc.app.data.local.entity.MealSlot?): Long {
    val now = LocalDateTime.now()
    val time = if (slot?.hasTargetTime == true) {
        LocalTime.of(slot.targetHour!!, slot.targetMinute!!)
    } else {
        now.toLocalTime().withSecond(0).withNano(0)
    }
    val candidate = LocalDate.ofEpochDay(epochDay).atTime(time)
    val today = LocalDate.now()
    val safeCandidate = if (epochDay == today.toEpochDay() && candidate.isAfter(now)) {
        today.atTime(now.hour, now.minute, now.second, now.nano)
    } else {
        candidate
    }
    return safeCandidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

internal fun normalizeMealTimeForDay(epochDay: Long, epochMillis: Long): Long {
    val now = System.currentTimeMillis()
    return if (epochDay == LocalDate.now().toEpochDay() && epochMillis > now) now else epochMillis
}

/** Keeps the source meal's local time while moving it to another epoch-day. */
internal fun moveMealTimeToDay(targetEpochDay: Long, sourceEpochMillis: Long): Long {
    val sourceTime = Instant.ofEpochMilli(sourceEpochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return normalizeMealTimeForDay(
        targetEpochDay,
        LocalDate.ofEpochDay(targetEpochDay)
            .atTime(sourceTime.hour, sourceTime.minute, sourceTime.second, sourceTime.nano)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
}
