package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.entity.MealSlot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MealTimeDefaultsTest {

    @Test
    fun `default time uses the system time on the selected day`() {
        val day = LocalDate.of(2026, 8, 16)
        val result = defaultMealTime(day.toEpochDay(), LocalDateTime.of(day, LocalTime.of(13, 15)))
        val local = Instant.ofEpochMilli(result).atZone(ZoneId.systemDefault())

        assertEquals(day, local.toLocalDate())
        assertEquals(13, local.hour)
        assertEquals(15, local.minute)
    }

    @Test
    fun `moving a meal preserves its local wall clock time`() {
        val source = LocalDate.of(2026, 8, 17)
            .atTime(16, 51)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val targetDay = LocalDate.of(2026, 8, 12)
        val moved = moveMealTimeToDay(targetDay.toEpochDay(), source)
        val local = Instant.ofEpochMilli(moved).atZone(ZoneId.systemDefault())

        assertEquals(targetDay, local.toLocalDate())
        assertEquals(16, local.hour)
        assertEquals(51, local.minute)
    }

    @Test
    fun `default time ignores a meal slot configuration`() {
        val today = LocalDate.of(2026, 8, 18)
        val result = defaultMealTime(today.toEpochDay(), LocalDateTime.of(today, LocalTime.of(9, 42)))
        val local = Instant.ofEpochMilli(result).atZone(ZoneId.systemDefault())

        assertEquals(today, local.toLocalDate())
        assertEquals(9, local.hour)
        assertEquals(42, local.minute)
    }
}
