package com.caloriecalc.app.domain

data class MicronutrientTarget(
    val label: String,
    val unit: String,
    val dailyTarget: Double,
    /** True for nutrients where the "target" is a ceiling to stay under, not a floor to hit. */
    val isUpperLimit: Boolean = false
)

data class DailyLimitWarning(
    val label: String,
    val consumedGrams: Double,
    val limitGrams: Double
)

/**
 * Generic adult daily reference values following FDA Nutrition Facts Daily Values, based on a
 * 2,000-calorie diet, plus common dietary reference values for nutrients without an upper label
 * limit. The FDA limit is for added sugars; this app currently records total sugar, so a sugar
 * warning is deliberately conservative when naturally occurring sugar is included.
 * Not personalized beyond sex where that's the standard distinction (iron, in particular,
 * differs a lot). Treat as a general compass, not medical advice.
 */
object MicronutrientReference {

    const val SUGAR_LIMIT_GRAMS = 50.0
    const val SATURATED_FAT_LIMIT_GRAMS = 20.0

    fun dailyLimitWarnings(sugarGrams: Double, saturatedFatGrams: Double): List<DailyLimitWarning> =
        buildList {
            if (sugarGrams > SUGAR_LIMIT_GRAMS) {
                add(DailyLimitWarning("Sugar", sugarGrams, SUGAR_LIMIT_GRAMS))
            }
            if (saturatedFatGrams > SATURATED_FAT_LIMIT_GRAMS) {
                add(DailyLimitWarning("Saturated fat", saturatedFatGrams, SATURATED_FAT_LIMIT_GRAMS))
            }
        }

    fun targets(sex: Sex): List<MicronutrientTarget> = listOf(
        MicronutrientTarget("Fiber", "g", 30.0),
        MicronutrientTarget("Sugar", "g", SUGAR_LIMIT_GRAMS, isUpperLimit = true),
        MicronutrientTarget("Saturated fat", "g", SATURATED_FAT_LIMIT_GRAMS, isUpperLimit = true),
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
