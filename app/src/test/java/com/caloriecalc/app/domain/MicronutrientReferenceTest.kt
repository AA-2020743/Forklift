package com.caloriecalc.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MicronutrientReferenceTest {

    @Test
    fun `uses FDA daily limits for sugar and saturated fat`() {
        val targets = MicronutrientReference.targets(Sex.MALE)

        assertEquals(50.0, targets.first { it.label == "Sugar" }.dailyTarget, 0.0)
        assertEquals(20.0, targets.first { it.label == "Saturated fat" }.dailyTarget, 0.0)
    }

    @Test
    fun `warns only when a daily limit is exceeded`() {
        assertTrue(MicronutrientReference.dailyLimitWarnings(50.0, 20.0).isEmpty())

        val warnings = MicronutrientReference.dailyLimitWarnings(55.5, 21.25)

        assertEquals(listOf("Sugar", "Saturated fat"), warnings.map { it.label })
        assertEquals(55.5, warnings[0].consumedGrams, 0.0)
        assertEquals(20.0, warnings[1].limitGrams, 0.0)
    }
}
