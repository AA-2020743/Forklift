package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.UserProfile
import kotlin.math.roundToInt

/**
 * Recommended daily water intake: a widely-cited ~35 mL per kg of bodyweight as a baseline,
 * with an activity bonus since sweat losses scale with how much you train — not a precise
 * physiological requirement, just a reasonable, adjustable starting point.
 */
object WaterCalculator {

    private const val ML_PER_KG = 35.0

    private fun activityBonusMl(activityLevel: ActivityLevel): Double = when (activityLevel) {
        ActivityLevel.SEDENTARY -> 0.0
        ActivityLevel.LIGHT -> 250.0
        ActivityLevel.MODERATE -> 500.0
        ActivityLevel.VERY_ACTIVE -> 750.0
        ActivityLevel.EXTRA_ACTIVE -> 1000.0
    }

    fun recommendedMl(profile: UserProfile): Int =
        (profile.bodyWeightKg * ML_PER_KG + activityBonusMl(profile.activityLevel)).roundToInt()
}
