package com.caloriecalc.app.domain

import kotlin.math.abs
import kotlin.math.max

data class FoodEnergyMismatch(
    val statedCalories: Double,
    val macroCalories: Double
)

/** Flags material label mismatches while allowing normal nutrition-label rounding. */
fun findFoodEnergyMismatch(
    statedCalories: Double?,
    proteinGrams: Double?,
    fatGrams: Double?,
    carbGrams: Double?
): FoodEnergyMismatch? {
    val values = listOf(statedCalories, proteinGrams, fatGrams, carbGrams)
    if (values.any { it == null || !it.isFinite() || it < 0.0 }) return null

    val stated = statedCalories!!
    val calculated = proteinGrams!! * 4.0 + carbGrams!! * 4.0 + fatGrams!! * 9.0
    val tolerance = max(10.0, max(stated, calculated) * 0.10)

    return if (abs(stated - calculated) > tolerance) {
        FoodEnergyMismatch(stated, calculated)
    } else {
        null
    }
}
