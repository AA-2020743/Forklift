package com.caloriecalc.app.data.seed

import com.caloriecalc.app.data.local.entity.MealSlot

object MealSlotSeedData {
    /** Default times are a starting point for protein-spacing reminders, freely adjustable. */
    val defaults: List<MealSlot> = listOf(
        MealSlot(name = "Breakfast", sortOrder = 0, isBuiltIn = true, targetHour = 8, targetMinute = 0),
        MealSlot(name = "Pre-workout", sortOrder = 1, isBuiltIn = true, targetHour = 11, targetMinute = 0),
        MealSlot(name = "Lunch", sortOrder = 2, isBuiltIn = true, targetHour = 13, targetMinute = 0),
        MealSlot(name = "Post-workout", sortOrder = 3, isBuiltIn = true, targetHour = 16, targetMinute = 0),
        MealSlot(name = "Dinner", sortOrder = 4, isBuiltIn = true, targetHour = 19, targetMinute = 0),
        MealSlot(name = "Snack", sortOrder = 5, isBuiltIn = true, targetHour = 21, targetMinute = 0)
    )
}
