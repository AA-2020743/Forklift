package com.caloriecalc.app.ui.dashboard

import com.caloriecalc.app.data.local.entity.MealSlot
import org.junit.Assert.assertEquals
import org.junit.Test

class MealSummaryOrderingTest {

    @Test
    fun `orders timed meals chronologically and keeps untimed meals in slot order`() {
        val breakfast = summary("Breakfast", sortOrder = 0, time = 9_00L)
        val lunch = summary("Lunch", sortOrder = 2, time = 12_30L)
        val snack = summary("Snack", sortOrder = 5, time = null)
        val dinner = summary("Dinner", sortOrder = 4, time = null)

        val result = orderMealSummaries(listOf(lunch, snack, breakfast, dinner))

        assertEquals(listOf("Breakfast", "Lunch", "Dinner", "Snack"), result.map { it.mealSlot.name })
    }

    private fun summary(name: String, sortOrder: Int, time: Long?) = MealSummary(
        mealSlot = MealSlot(name = name, sortOrder = sortOrder),
        calories = 0,
        proteinGrams = 0.0,
        itemCount = 0,
        consumedAtEpochMillis = time
    )
}
