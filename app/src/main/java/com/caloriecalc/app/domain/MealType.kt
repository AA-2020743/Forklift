package com.caloriecalc.app.domain

/** The meal slots a food entry can be logged under. */
enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack"),
    PRE_WORKOUT("Pre-workout"),
    POST_WORKOUT("Post-workout");

    companion object {
        val entriesInDayOrder = listOf(BREAKFAST, PRE_WORKOUT, LUNCH, POST_WORKOUT, DINNER, SNACK)
    }
}
