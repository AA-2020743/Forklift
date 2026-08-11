package com.caloriecalc.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector
import com.caloriecalc.app.domain.MealType

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Workouts : Screen("workouts")
    data object Weight : Screen("weight")
    data object Profile : Screen("profile")

    data object MealLog : Screen("meal_log/{mealType}") {
        fun createRoute(mealType: MealType) = "meal_log/${mealType.name}"
    }

    data object AddFood : Screen("add_food/{mealType}") {
        fun createRoute(mealType: MealType) = "add_food/${mealType.name}"
    }

    data object BarcodeScan : Screen("barcode_scan/{mealType}") {
        fun createRoute(mealType: MealType) = "barcode_scan/${mealType.name}"
    }

    data object ManualFoodEntry : Screen("manual_food_entry/{mealType}") {
        fun createRoute(mealType: MealType) = "manual_food_entry/${mealType.name}"
    }

    data object Quantity : Screen("quantity/{foodId}/{mealType}") {
        fun createRoute(foodId: Long, mealType: MealType) = "quantity/$foodId/${mealType.name}"
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
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Today", Icons.Filled.RestaurantMenu),
    BottomNavItem(Screen.Workouts, "Lifting", Icons.Filled.FitnessCenter),
    BottomNavItem(Screen.Weight, "Weight", Icons.Filled.MonitorWeight),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person)
)
