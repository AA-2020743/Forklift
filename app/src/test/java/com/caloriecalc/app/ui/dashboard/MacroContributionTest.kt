package com.caloriecalc.app.ui.dashboard

import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.MealEntry
import com.caloriecalc.app.data.repository.MealEntryWithFood
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
            loggedAsServing = true,
            servingSizeGrams = 125.0,
            servingName = "pot"
        )

        assertEquals(3.0, contribution.grams / contribution.servingSizeGrams!!, 0.0)
    }

    @Test
    fun `keeps gram and serving logs separate for the same food`() {
        val food = FoodItem(
            id = 7,
            name = "Smeerkaas",
            caloriesPer100g = 200.0,
            proteinPer100g = 10.0,
            fatPer100g = 10.0,
            carbsPer100g = 10.0,
            servingSizeGrams = 20.0
        )
        val gramEntry = MealEntry(
            foodItemId = food.id,
            mealSlotId = 1,
            epochDay = 1,
            loggedAtEpochMillis = 1,
            grams = 40.0,
            protein = 4.0,
            calories = 80.0,
            fat = 4.0,
            carbs = 4.0
        )
        val servingEntry = gramEntry.copy(
            mealSlotId = 2,
            loggedAsServing = true,
            loggedServingSizeGrams = 20.0
        )

        val result = macroContributions(
            entries = listOf(MealEntryWithFood(gramEntry, food), MealEntryWithFood(servingEntry, food)),
            mealNameById = mapOf(1L to "Lunch", 2L to "Dinner"),
            macroOf = { it.protein }
        )

        assertEquals(2, result.size)
        assertEquals(listOf(false, true), result.map { it.loggedAsServing }.sorted())
    }
}
