package com.caloriecalc.app.domain

data class MicronutrientTarget(
    val label: String,
    val unit: String,
    val dailyTarget: Double?,
    /** True for nutrients where the "target" is a ceiling to stay under, not a floor to hit. */
    val isUpperLimit: Boolean = false
)

data class DailyLimitWarning(
    val label: String,
    val consumedGrams: Double,
    val limitGrams: Double
)

data class DailyLimitReferences(
    val addedSugarGrams: Double,
    val saturatedFatGrams: Double
)

/**
 * Generic adult daily reference values. Added sugar and saturated fat use the widely used 10%
 * calorie ceiling: 4 kcal/g for sugar and 9 kcal/g for fat. The older fixed 2,000-calorie label
 * values are therefore only the result at 2,000 calories, not a suitable limit for every user.
 * Not personalized beyond sex where that's the standard distinction (iron, in particular,
 * differs a lot). Treat as a general compass, not medical advice.
 */
object MicronutrientReference {

    const val CALORIE_LIMIT_PERCENT = 0.10
    private const val SUGAR_KCAL_PER_GRAM = 4.0
    private const val FAT_KCAL_PER_GRAM = 9.0
    const val STANDARD_REFERENCE_CALORIES = 2_000

    fun dailyLimits(calorieTarget: Int): DailyLimitReferences {
        val calories = calorieTarget.coerceAtLeast(0).toDouble()
        return DailyLimitReferences(
            addedSugarGrams = calories * CALORIE_LIMIT_PERCENT / SUGAR_KCAL_PER_GRAM,
            saturatedFatGrams = calories * CALORIE_LIMIT_PERCENT / FAT_KCAL_PER_GRAM
        )
    }

    fun dailyLimitWarnings(
        addedSugarGrams: Double?,
        saturatedFatGrams: Double,
        calorieTarget: Int
    ): List<DailyLimitWarning> {
        val limits = dailyLimits(calorieTarget)
        return buildList {
            if (addedSugarGrams != null && addedSugarGrams > limits.addedSugarGrams) {
                add(DailyLimitWarning("Added sugar", addedSugarGrams, limits.addedSugarGrams))
            }
            if (saturatedFatGrams > limits.saturatedFatGrams) {
                add(DailyLimitWarning("Saturated fat", saturatedFatGrams, limits.saturatedFatGrams))
            }
        }
    }

    fun targets(
        sex: Sex,
        calorieTarget: Int = STANDARD_REFERENCE_CALORIES
    ): List<MicronutrientTarget> {
        val limits = dailyLimits(calorieTarget)
        return listOf(
            MicronutrientTarget("Fiber", "g", 30.0),
            MicronutrientTarget("Total sugar", "g", null),
            MicronutrientTarget("Added sugar", "g", limits.addedSugarGrams, isUpperLimit = true),
            MicronutrientTarget("Saturated fat", "g", limits.saturatedFatGrams, isUpperLimit = true),
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
}
