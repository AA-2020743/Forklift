package com.caloriecalc.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Workouts : Screen("workouts")
    data object Weight : Screen("weight")
    data object Profile : Screen("profile")
    data object Micronutrients : Screen("micronutrients")

    data object MealLog : Screen("meal_log/{mealSlotId}") {
        fun createRoute(mealSlotId: Long) = "meal_log/$mealSlotId"
    }

    data object AddFood : Screen("add_food/{mealSlotId}") {
        fun createRoute(mealSlotId: Long) = "add_food/$mealSlotId"
    }

    data object BarcodeScan : Screen("barcode_scan/{mealSlotId}") {
        fun createRoute(mealSlotId: Long) = "barcode_scan/$mealSlotId"
    }

    data object ManualFoodEntry : Screen("manual_food_entry/{mealSlotId}") {
        fun createRoute(mealSlotId: Long) = "manual_food_entry/$mealSlotId"
    }

    data object PhotoEstimate : Screen("photo_estimate/{mealSlotId}") {
        fun createRoute(mealSlotId: Long) = "photo_estimate/$mealSlotId"
    }

    data object Quantity : Screen("quantity/{foodId}/{mealSlotId}") {
        fun createRoute(foodId: Long, mealSlotId: Long) = "quantity/$foodId/$mealSlotId"
    }

    data object EditFood : Screen("edit_food/{foodId}") {
        fun createRoute(foodId: Long) = "edit_food/$foodId"
    }

    data object WorkoutSessionDetail : Screen("workout_session/{sessionId}") {
        fun createRoute(sessionId: Long) = "workout_session/$sessionId"
    }

    data object ExercisePicker : Screen("exercise_picker/{sessionId}") {
        fun createRoute(sessionId: Long) = "exercise_picker/$sessionId"
    }

    data object LogSet : Screen("log_set/{sessionId}/{exerciseId}") {
        fun createRoute(sessionId: Long, exerciseId: Long) = "log_set/$sessionId/$exerciseId"
    }

    data object ExerciseHistory : Screen("exercise_history/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "exercise_history/$exerciseId"
    }

    data object Templates : Screen("templates")

    data object TemplateEditor : Screen("template_editor/{templateId}") {
        fun createRoute(templateId: Long) = "template_editor/$templateId"
    }

    data object TemplateExercisePicker : Screen("template_exercise_picker/{templateId}") {
        fun createRoute(templateId: Long) = "template_exercise_picker/$templateId"
    }
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Today", Icons.Filled.RestaurantMenu),
    BottomNavItem(Screen.Workouts, "Lifting", Icons.Filled.FitnessCenter),
    BottomNavItem(Screen.Weight, "Weight", Icons.Filled.MonitorWeight),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person)
)
