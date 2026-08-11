package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.UserProfile
import kotlin.math.max
import kotlin.math.roundToInt

data class NutritionTargets(
    val calorieTarget: Int,
    val proteinTargetGrams: Double,
    val fatMinGrams: Double,
    val fatTargetGrams: Double,
    val fatMaxGrams: Double,
    val carbTargetGrams: Double
)

/**
 * Derives daily calorie and macro targets from body stats.
 *
 * - BMR via Mifflin-St Jeor.
 * - Protein target follows the ~2.3 g/kg guidance for people who lift (configurable per profile).
 * - Fat has a safe floor/ceiling expressed as a share of total calories (default 20-35%);
 *   the target used for the carb split is the midpoint of that range.
 * - Carbs take whatever calorie budget remains after protein and fat.
 */
object NutritionCalculator {

    private const val MIN_SAFE_CALORIES = 1200

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

        val proteinTargetGrams = profile.proteinGramsPerKg * profile.bodyWeightKg
        val proteinCalories = proteinTargetGrams * 4

        val fatMinGrams = (calorieTarget * profile.fatPercentMin) / 9
        val fatMaxGrams = (calorieTarget * profile.fatPercentMax) / 9
        val fatTargetGrams = (fatMinGrams + fatMaxGrams) / 2

        val remainingCaloriesForCarbs = calorieTarget - proteinCalories - (fatTargetGrams * 9)
        val carbTargetGrams = max(0.0, remainingCaloriesForCarbs / 4)

        return NutritionTargets(
            calorieTarget = calorieTarget,
            proteinTargetGrams = proteinTargetGrams,
            fatMinGrams = fatMinGrams,
            fatTargetGrams = fatTargetGrams,
            fatMaxGrams = fatMaxGrams,
            carbTargetGrams = carbTargetGrams
        )
    }
}
