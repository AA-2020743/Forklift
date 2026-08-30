package com.caloriecalc.app.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class MacroContributionTest {

    @Test
    fun `combined serving quantity is derived from total grams`() {
        val contribution = MacroContribution(
            foodId = 7,
            foodName = "Yogurt",
            mealName = "Breakfast, Snack",
            grams = 375.0,
            macroGrams = 30.0,
            servingSizeGrams = 125.0,
            servingName = "pot"
        )

        assertEquals(3.0, contribution.grams / contribution.servingSizeGrams!!, 0.0)
    }
}
