package com.caloriecalc.app.domain

import kotlin.math.roundToInt

/**
 * Quick-log cardio/misc activity types, each carrying a MET (metabolic equivalent) value used
 * to estimate calories burned via [ActivityCalculator]. MET values are rough, widely-cited
 * averages for a moderate pace/intensity, not adjusted for individual effort level.
 */
enum class ActivityType(val displayName: String, val met: Double, val tracksSteps: Boolean = false) {
    WALKING("Walking", 3.5, tracksSteps = true),
    RUNNING("Running", 9.8, tracksSteps = true),
    HIKING("Hiking", 6.0),
    CYCLING("Cycling", 7.5),
    SWIMMING("Swimming", 8.0),
    BOXING("Boxing", 9.0),
    SPORTS("Sports", 7.0),
    YOGA("Yoga", 3.0),
    HIIT("HIIT", 8.0),
    OTHER("Other", 4.0)
}

/** kcal = MET * bodyWeightKg * hours — a standard, widely-used estimate for activity burn. */
object ActivityCalculator {

    /** MET for general moderate resistance training, used once a lifting session has a known
     * duration (see WorkoutSession.endedAtEpochMillis) to estimate its calorie burn. */
    const val LIFTING_MET = 5.0

    fun estimateCaloriesBurned(met: Double, durationMinutes: Int, bodyWeightKg: Double): Int =
        (met * bodyWeightKg * durationMinutes / 60.0).roundToInt()
}
