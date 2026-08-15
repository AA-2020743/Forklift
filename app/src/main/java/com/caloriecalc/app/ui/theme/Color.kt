package com.caloriecalc.app.ui.theme

import androidx.compose.ui.graphics.Color

val GreenPrimary = Color(0xFF2E7D32)
val GreenPrimaryDark = Color(0xFF81C784)
val OrangeSecondary = Color(0xFFEF6C00)
val BlueTertiary = Color(0xFF1565C0)

val StatusBelowThreshold = Color(0xFFD32F2F)
val StatusApproaching = Color(0xFFF9A825)
val StatusOnTarget = Color(0xFF2E7D32)
val StatusOverTarget = Color(0xFF1565C0)

val LightBackground = Color(0xFFFAFAF7)
val LightSurface = Color(0xFFFFFFFF)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)

// Meal-icon accent palette: fixed, non-theme-adaptive tones used only as icon-badge tints
// (same idea as the Status colors above), chosen to loosely evoke each meal's mood.
val MealAccentBreakfast = Color(0xFFF5A623) // sunrise amber
val MealAccentLunch = Color(0xFF4CAF50) // fresh green
val MealAccentDinner = Color(0xFF5C6BC0) // dusk indigo
val MealAccentSnack = Color(0xFFEC6FA6) // playful pink
val MealAccentPreWorkout = Color(0xFFFFB300) // energy amber
val MealAccentPostWorkout = Color(0xFF26A69A) // recovery teal

/** Rotating fallback palette for custom meal names that don't match a known keyword. */
val MealAccentPalette = listOf(
    Color(0xFF8D6E63),
    Color(0xFF7E57C2),
    Color(0xFF26C6DA),
    Color(0xFFEF5350),
    Color(0xFF9CCC65),
    Color(0xFFFFA726)
)
