package com.caloriecalc.app.reminder

import kotlin.math.ceil

data class ProteinGapTiming(
    val isDue: Boolean,
    val elapsedMinutes: Long?,
    val nextDelayMinutes: Long
)

/** Calculates the next check from the consumed time rather than an unrelated hourly cadence. */
fun calculateProteinGapTiming(
    nowMillis: Long,
    lastProteinMealMillis: Long?,
    gapHours: Int,
    overdueRecheckMinutes: Long = ReminderScheduler.PROTEIN_CHECK_INTERVAL_MINUTES
): ProteinGapTiming {
    if (lastProteinMealMillis == null) {
        return ProteinGapTiming(false, null, overdueRecheckMinutes)
    }

    val elapsedMillis = (nowMillis - lastProteinMealMillis).coerceAtLeast(0)
    val elapsedMinutes = elapsedMillis / 60_000L
    val dueAtMillis = lastProteinMealMillis + gapHours * 3_600_000L
    val remainingMillis = dueAtMillis - nowMillis

    return if (remainingMillis <= 0) {
        ProteinGapTiming(true, elapsedMinutes, overdueRecheckMinutes)
    } else {
        ProteinGapTiming(
            isDue = false,
            elapsedMinutes = elapsedMinutes,
            nextDelayMinutes = ceil(remainingMillis / 60_000.0).toLong().coerceAtLeast(1)
        )
    }
}
