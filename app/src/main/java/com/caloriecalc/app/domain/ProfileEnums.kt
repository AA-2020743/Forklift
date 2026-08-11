package com.caloriecalc.app.domain

enum class Sex { MALE, FEMALE }

enum class ActivityLevel(val multiplier: Double, val displayName: String) {
    SEDENTARY(1.2, "Sedentary (little/no exercise)"),
    LIGHT(1.375, "Lightly active (1-3 days/week)"),
    MODERATE(1.55, "Moderately active (3-5 days/week)"),
    VERY_ACTIVE(1.725, "Very active (6-7 days/week)"),
    EXTRA_ACTIVE(1.9, "Extra active (physical job + training)")
}

enum class Goal(val calorieAdjustment: Int, val displayName: String) {
    LOSE(-500, "Lose weight"),
    MAINTAIN(0, "Maintain weight"),
    GAIN(300, "Gain weight")
}
