package com.caloriecalc.app.ui.components

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Combines a day (as an epoch-day) with a wall-clock time into an epoch-millis instant in the
 * device's time zone. Used when logging a meal's "eaten at" time so protein-spacing reminders
 * anchor on when you actually ate rather than when you happened to open the app.
 */
fun epochMillisForMealTime(epochDay: Long, hour: Int, minute: Int): Long =
    LocalDate.ofEpochDay(epochDay).atTime(hour, minute)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Hour/minute of the wall-clock time at [epochMillis], in the device's time zone. */
fun mealTimeParts(epochMillis: Long): Pair<Int, Int> {
    val time = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
    return time.hour to time.minute
}

/** Zero-padded 24h clock label, e.g. "08:05". */
fun formatClock(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute)

fun formatMealTime(epochMillis: Long): String =
    mealTimeParts(epochMillis).let { formatClock(it.first, it.second) }
