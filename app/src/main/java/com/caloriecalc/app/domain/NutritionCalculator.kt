package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.UserProfile
import kotlin.math.max
import kotlin.math.roundToInt

data class MacroRange(val minGrams: Double, val maxGrams: Double)

data class NutritionTargets(
    val calorieTarget: Int,
    val protein: MacroRange,
    val fat: MacroRange,
    val carbs: MacroRange
)

/**
 * Derives daily calorie and macro RANGES (not single numbers to hit exactly) from body stats.
 *
 * - BMR via Mifflin-St Jeor; TDEE = BMR * activity multiplier; calorie target = TDEE + goal
 *   adjustment (each [Goal] carries its own adjustment, including body-recomposition variants).
 * - Protein is a g/kg range (default 1.6-2.4 g/kg, the span generally supported by research
 *   for resistance-trained individuals), not a single target.
 * - Fat is a percent-of-calories range. For male sex, the floor is clamped to at least 20% of
 *   calories regardless of the user's own setting: diets below roughly that share of fat are
 *   associated with reduced testosterone in men, so this app won't recommend going lower.
 * - Carbs fill whatever's left of the calorie budget: the carb *ceiling* comes from protein/fat
 *   sitting at their minimums (most room left for carbs), and the carb *floor* comes from
 *   protein/fat sitting at their maximums (least room left).
 */
object NutritionCalculator {

    private const val MIN_SAFE_CALORIES = 1200

    /** Diets below ~20% of calories from fat are linked to lower testosterone in men. */
    private const val MALE_FAT_PERCENT_FLOOR = 0.20

    fun computeBmr(profile: UserProfile): Double {
        val base = 10 * profile.bodyWeightKg + 6.25 * profile.heightCm - 5 * profile.age
        return when (profile.sex) {
            Sex.MALE -> base + 5
            Sex.FEMALE -> base - 161
        }
    }

    fun computeTdee(profile: UserProfile): Double = computeBmr(profile) * profile.activityLevel.multiplier

    fun computeTargets(profile: UserProfile): NutritionTargets {
        val tdee = computeTdee(profile)
        val calorieTarget = profile.manualCalorieTarget
            ?: max(MIN_SAFE_CALORIES, (tdee + profile.goal.calorieAdjustment).roundToInt())

        val fatPercentMin = if (profile.sex == Sex.MALE) {
            max(profile.fatPercentMin, MALE_FAT_PERCENT_FLOOR)
        } else {
            profile.fatPercentMin
        }
        val fatPercentMax = max(profile.fatPercentMax, fatPercentMin)
        val fatMinGrams = calorieTarget * fatPercentMin / 9
        val fatMaxGrams = calorieTarget * fatPercentMax / 9

        val proteinMinGrams = profile.proteinMinGramsPerKg * profile.bodyWeightKg
        val proteinMaxGrams = max(profile.proteinMaxGramsPerKg, profile.proteinMinGramsPerKg) * profile.bodyWeightKg

        val carbMaxGrams = max(0.0, (calorieTarget - proteinMinGrams * 4 - fatMinGrams * 9) / 4)
        val carbMinGrams = max(0.0, (calorieTarget - proteinMaxGrams * 4 - fatMaxGrams * 9) / 4)
            .coerceAtMost(carbMaxGrams)

        return NutritionTargets(
            calorieTarget = calorieTarget,
            protein = MacroRange(proteinMinGrams, proteinMaxGrams),
            fat = MacroRange(fatMinGrams, fatMaxGrams),
            carbs = MacroRange(carbMinGrams, carbMaxGrams)
        )
    }
}
