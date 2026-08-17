package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.entity.MealSlot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MealTimeDefaultsTest {

    @Test
    fun `default time uses the meal slot time on a historical day`() {
        val day = LocalDate.now().minusDays(2)
        val result = defaultMealTime(
            day.toEpochDay(),
            MealSlot(name = "Lunch", sortOrder = 0, targetHour = 13, targetMinute = 15)
        )
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
}
