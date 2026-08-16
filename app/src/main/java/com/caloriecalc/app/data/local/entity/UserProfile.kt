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
    val weightReminderMinute: Int = 0,
    /**
     * Protein-spacing reminders: muscle protein synthesis responds to a dose of protein and
     * then falls back toward baseline, so research on distribution generally favours spreading
     * intake across the day rather than one large hit. [proteinGapHours] is how long a gap is
     * allowed before a nudge; [proteinDoseGrams] is the minimum amount that counts as "fed",
     * so a black coffee or an apple doesn't reset the clock.
     */
    val proteinReminderEnabled: Boolean = true,
    val proteinGapHours: Int = 4,
    val proteinDoseGrams: Double = 15.0,
    /**
     * Waking window, used to hold every reminder until you're actually up. Stored as
     * wake/sleep wall-clock times and compared with wrap-around, so a sleep time past midnight
     * behaves correctly.
     */
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val sleepHour: Int = 23,
    val sleepMinute: Int = 0
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
