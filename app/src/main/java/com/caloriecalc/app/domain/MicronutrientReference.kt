package com.caloriecalc.app.domain

data class MicronutrientTarget(
    val label: String,
    val unit: String,
    val dailyTarget: Double,
    /** True for nutrients where the "target" is a ceiling to stay under, not a floor to hit. */
    val isUpperLimit: Boolean = false
)

/**
 * Generic adult daily reference values (roughly following common US/EU dietary guidelines).
 * Not personalized beyond sex where that's the standard distinction (iron, in particular,
 * differs a lot). Treat as a general compass, not medical advice.
 */
object MicronutrientReference {

    fun targets(sex: Sex): List<MicronutrientTarget> = listOf(
        MicronutrientTarget("Fiber", "g", 30.0),
        MicronutrientTarget("Sugar", "g", 50.0, isUpperLimit = true),
        MicronutrientTarget("Saturated fat", "g", 22.0, isUpperLimit = true),
        MicronutrientTarget("Sodium", "mg", 2300.0, isUpperLimit = true),
        MicronutrientTarget("Potassium", "mg", if (sex == Sex.MALE) 3400.0 else 2600.0),
        MicronutrientTarget("Calcium", "mg", 1000.0),
        MicronutrientTarget("Iron", "mg", if (sex == Sex.MALE) 8.0 else 18.0),
        MicronutrientTarget("Vitamin C", "mg", if (sex == Sex.MALE) 90.0 else 75.0),
        MicronutrientTarget("Vitamin D", "mcg", 15.0),
        MicronutrientTarget("Vitamin B12", "mcg", 2.4),
        MicronutrientTarget("Magnesium", "mg", if (sex == Sex.MALE) 400.0 else 310.0),
        MicronutrientTarget("Zinc", "mg", if (sex == Sex.MALE) 11.0 else 8.0)
    )
}
