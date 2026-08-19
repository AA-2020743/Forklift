package com.caloriecalc.app.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProteinGapTimingTest {

    @Test
    fun `three hour gap schedules from meal consumed time`() {
        val mealTime = 1_000_000L
        val now = mealTime + 30 * 60_000L

        val timing = calculateProteinGapTiming(now, mealTime, gapHours = 3)

        assertFalse(timing.isDue)
        assertEquals(150L, timing.nextDelayMinutes)
    }

    @Test
    fun `check just before deadline schedules remaining minute`() {
        val mealTime = 1_000_000L
        val now = mealTime + 179 * 60_000L

        val timing = calculateProteinGapTiming(now, mealTime, gapHours = 3)

        assertFalse(timing.isDue)
        assertEquals(1L, timing.nextDelayMinutes)
    }

    @Test
    fun `check at deadline is due`() {
        val mealTime = 1_000_000L
        val now = mealTime + 180 * 60_000L

        val timing = calculateProteinGapTiming(now, mealTime, gapHours = 3)

        assertTrue(timing.isDue)
        assertEquals(180L, timing.elapsedMinutes)
        assertEquals(ReminderScheduler.PROTEIN_CHECK_INTERVAL_MINUTES, timing.nextDelayMinutes)
    }
}
