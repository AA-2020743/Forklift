package com.caloriecalc.app.data.seed

import com.caloriecalc.app.data.local.entity.MealSlot

object MealSlotSeedData {
    val defaults: List<MealSlot> = listOf(
        MealSlot(name = "Breakfast", sortOrder = 0, isBuiltIn = true),
        MealSlot(name = "Pre-workout", sortOrder = 1, isBuiltIn = true),
        MealSlot(name = "Lunch", sortOrder = 2, isBuiltIn = true),
        MealSlot(name = "Post-workout", sortOrder = 3, isBuiltIn = true),
        MealSlot(name = "Dinner", sortOrder = 4, isBuiltIn = true),
        MealSlot(name = "Snack", sortOrder = 5, isBuiltIn = true)
    )
}
