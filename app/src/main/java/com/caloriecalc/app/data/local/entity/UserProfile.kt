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
    /** Grams of protein per kg of body weight. Default follows the 2.3 g/kg guidance for lifters. */
    val proteinGramsPerKg: Double = 2.3,
    /** Minimum share of total calories that should come from fat, as a fraction (e.g. 0.20 = 20%). */
    val fatPercentMin: Double = 0.20,
    val fatPercentMax: Double = 0.35,
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
