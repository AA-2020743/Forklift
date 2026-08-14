package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.caloriecalc.app.domain.ActivityLevel
import com.caloriecalc.app.domain.Goal
import com.caloriecalc.app.domain.Sex

/**
 * Singleton row (fixed id = 1) holding the user's body stats and nutrition target overrides.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val bodyWeightKg: Double = 75.0,
    val heightCm: Double = 175.0,
    val age: Int = 30,
    val sex: Sex = Sex.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: Goal = Goal.MAINTAIN,
    /**
     * Protein range in grams per kg of body weight. Defaults (1.6-2.4 g/kg) span the range
     * generally supported by research for resistance-trained individuals; [NutritionCalculator]
     * turns this into an absolute gram range rather than a single number to hit.
     */
    val proteinMinGramsPerKg: Double = 1.6,
    val proteinMaxGramsPerKg: Double = 2.4,
    /**
     * Fat range in grams per kg of body weight: 0.6 g/kg is a commonly cited floor, 0.8-1.0 g/kg
     * a "good" target. [NutritionCalculator] additionally enforces a hard biological floor for
     * male sex (dietary fat below ~20% of calories is associated with reduced testosterone),
     * expressed in grams and taken as a lower bound alongside this g/kg minimum.
     */
    val fatMinGramsPerKg: Double = 0.6,
    val fatMaxGramsPerKg: Double = 1.0,
    /** Manual override for daily calorie target; null means derive it from BMR/TDEE + goal. */
    val manualCalorieTarget: Int? = null,
    val weightReminderEnabled: Boolean = true,
    val weightReminderHour: Int = 8,
    val weightReminderMinute: Int = 0
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
