package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.Micronutrients
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MicronutrientReferenceTest {

    @Test
    fun `calculates ten percent limits from the calorie target`() {
        val targets = MicronutrientReference.targets(Sex.MALE, calorieTarget = 2_400)
        val addedSugar = targets.first { it.label == "Added sugar" }.dailyTarget
        val saturatedFat = targets.first { it.label == "Saturated fat" }.dailyTarget

        assertEquals(60.0, addedSugar!!, 0.0)
        assertEquals(2_400.0 * 0.10 / 9.0, saturatedFat!!, 0.0001)
        assertTrue(targets.first { it.label == "Total sugar" }.dailyTarget == null)
    }

    @Test
    fun `warns only when a daily limit is exceeded`() {
        assertTrue(
            MicronutrientReference.dailyLimitWarnings(
                addedSugarGrams = 60.0,
                saturatedFatGrams = 2_400.0 * 0.10 / 9.0,
                calorieTarget = 2_400
            ).isEmpty()
        )

        val warnings = MicronutrientReference.dailyLimitWarnings(
            addedSugarGrams = 60.1,
            saturatedFatGrams = 27.0,
            calorieTarget = 2_400
        )

        assertEquals(listOf("Added sugar", "Saturated fat"), warnings.map { it.label })
        assertEquals(60.1, warnings[0].consumedGrams, 0.0)
        assertEquals(2_400.0 * 0.10 / 9.0, warnings[1].limitGrams, 0.0001)
    }

    @Test
    fun `does not create a warning from total sugar`() {
        val totalSugarOnly = Micronutrients(sugarGrams = 100.0)
        val warnings = MicronutrientReference.dailyLimitWarnings(
            addedSugarGrams = totalSugarOnly.addedSugarGrams,
            saturatedFatGrams = 0.0,
            calorieTarget = 2_400
        )

        assertTrue(warnings.isEmpty())
    }
}
