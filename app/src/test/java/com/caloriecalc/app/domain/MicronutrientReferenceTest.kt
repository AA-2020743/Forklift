package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.Micronutrients
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MicronutrientReferenceTest {

    @Test
    fun `calculates ten percent limits from the calorie target`() {
        val targets = MicronutrientReference.targets(Sex.MALE, calorieTarget = 2_400)
        val sugar = targets.first { it.label == "Sugar" }.dailyTarget
        val saturatedFat = targets.first { it.label == "Saturated fat" }.dailyTarget

        assertEquals(60.0, sugar, 0.0)
        assertEquals(2_400.0 * 0.10 / 9.0, saturatedFat, 0.0001)
    }

    @Test
    fun `warns only when a daily limit is exceeded`() {
        assertTrue(
            MicronutrientReference.dailyLimitWarnings(
                sugarGrams = 60.0,
                saturatedFatGrams = 2_400.0 * 0.10 / 9.0,
                calorieTarget = 2_400
            ).isEmpty()
        )

        val warnings = MicronutrientReference.dailyLimitWarnings(
            sugarGrams = 60.1,
            saturatedFatGrams = 27.0,
            calorieTarget = 2_400
        )

        assertEquals(listOf("Sugar", "Saturated fat"), warnings.map { it.label })
        assertEquals(60.1, warnings[0].consumedGrams, 0.0)
        assertEquals(2_400.0 * 0.10 / 9.0, warnings[1].limitGrams, 0.0001)
    }

    @Test
    fun `uses the combined sugar value for the warning`() {
        val combinedSugar = Micronutrients(sugarGrams = 60.1)
        val warnings = MicronutrientReference.dailyLimitWarnings(
            sugarGrams = combinedSugar.sugarGrams!!,
            saturatedFatGrams = 0.0,
            calorieTarget = 2_400
        )

        assertEquals(listOf("Sugar"), warnings.map { it.label })
    }
}
