package com.caloriecalc.app.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class MicronutrientsTest {

    @Test
    fun `assumes the sugar label is added sugar and keeps total sugar inclusive`() {
        val normalized = Micronutrients(sugarGrams = 12.0)
            .withAddedSugarAssumedFromTotal()

        assertEquals(12.0, normalized.sugarGrams!!, 0.0)
        assertEquals(12.0, normalized.addedSugarGrams!!, 0.0)
    }

    @Test
    fun `raises total sugar when explicit added sugar is higher`() {
        val normalized = Micronutrients(sugarGrams = 4.0, addedSugarGrams = 6.0)
            .withAddedSugarAssumedFromTotal()

        assertEquals(6.0, normalized.sugarGrams!!, 0.0)
        assertEquals(6.0, normalized.addedSugarGrams!!, 0.0)
    }
}
