package com.caloriecalc.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FoodEnergyCheckTest {

    @Test
    fun `aligned calories and macros are accepted`() {
        assertNull(findFoodEnergyMismatch(200.0, 20.0, 8.0, 12.0))
    }

    @Test
    fun `normal label rounding is accepted`() {
        assertNull(findFoodEnergyMismatch(210.0, 20.0, 8.0, 12.0))
    }

    @Test
    fun `material mismatch is flagged with calculated calories`() {
        val mismatch = findFoodEnergyMismatch(300.0, 10.0, 5.0, 20.0)

        assertNotNull(mismatch)
        assertEquals(165.0, mismatch!!.macroCalories, 0.001)
    }
}
