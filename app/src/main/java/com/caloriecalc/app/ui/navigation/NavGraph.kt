package com.caloriecalc.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.caloriecalc.app.ui.dashboard.DashboardScreen
import com.caloriecalc.app.ui.foodlog.AddFoodScreen
import com.caloriecalc.app.ui.foodlog.BarcodeScanScreen
import com.caloriecalc.app.ui.foodlog.ManualFoodEntryScreen
import com.caloriecalc.app.ui.foodlog.MealLogScreen
import com.caloriecalc.app.ui.foodlog.QuantityScreen
import com.caloriecalc.app.ui.profile.ProfileScreen
import com.caloriecalc.app.ui.weight.WeightScreen
import com.caloriecalc.app.ui.workout.ExerciseHistoryScreen
import com.caloriecalc.app.ui.workout.ExercisePickerScreen
import com.caloriecalc.app.ui.workout.LogSetScreen
import com.caloriecalc.app.ui.workout.WorkoutListScreen
import com.caloriecalc.app.ui.workout.WorkoutSessionScreen

private val topLevelRoutes: Set<String> = bottomNavItems.map { it.screen.route }.toSet()

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(onMealClick = { mealSlotId ->
                    navController.navigate(Screen.MealLog.createRoute(mealSlotId))
                })
            }
            composable(Screen.Workouts.route) {
                WorkoutListScreen(onSessionClick = { id ->
                    navController.navigate(Screen.WorkoutSessionDetail.createRoute(id))
                })
            }
            composable(Screen.Weight.route) { WeightScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }

            composable(
                route = Screen.MealLog.route,
                arguments = listOf(navArgument("mealSlotId") { type = NavType.LongType })
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                MealLogScreen(
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onAddFood = { navController.navigate(Screen.AddFood.createRoute(it)) }
                )
            }

            composable(
                route = Screen.AddFood.route,
                arguments = listOf(navArgument("mealSlotId") { type = NavType.LongType })
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                AddFoodScreen(
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onScanBarcode = { navController.navigate(Screen.BarcodeScan.createRoute(it)) },
                    onManualEntry = { navController.navigate(Screen.ManualFoodEntry.createRoute(it)) },
                    onFoodSelected = { foodId, mid -> navController.navigate(Screen.Quantity.createRoute(foodId, mid)) }
                )
            }

            composable(
                route = Screen.BarcodeScan.route,
                arguments = listOf(navArgument("mealSlotId") { type = NavType.LongType })
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                BarcodeScanScreen(
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onManualEntry = { navController.navigate(Screen.ManualFoodEntry.createRoute(it)) },
                    onSearchByName = { navController.popBackStack() },
                    onFoodResolved = { foodId, mid -> navController.navigate(Screen.Quantity.createRoute(foodId, mid)) }
                )
            }

            composable(
                route = Screen.ManualFoodEntry.route,
                arguments = listOf(navArgument("mealSlotId") { type = NavType.LongType })
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                ManualFoodEntryScreen(
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onSaved = { foodId, mid -> navController.navigate(Screen.Quantity.createRoute(foodId, mid)) }
                )
            }

            composable(
                route = Screen.Quantity.route,
                arguments = listOf(
                    navArgument("foodId") { type = NavType.LongType },
                    navArgument("mealSlotId") { type = NavType.LongType }
                )
            ) { entry ->
                val foodId = entry.arguments?.getLong("foodId") ?: 0L
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                QuantityScreen(
                    foodId = foodId,
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onLogged = { navController.popBackStack(Screen.MealLog.createRoute(mealSlotId), false) }
                )
            }

            composable(
                route = Screen.WorkoutSessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { entry ->
                val sessionId = entry.arguments?.getLong("sessionId") ?: 0L
                WorkoutSessionScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate(Screen.ExercisePicker.createRoute(it)) },
                    onExerciseClick = { sid, exId -> navController.navigate(Screen.LogSet.createRoute(sid, exId)) },
                    onExerciseHistory = { exId -> navController.navigate(Screen.ExerciseHistory.createRoute(exId)) }
                )
            }

            composable(
                route = Screen.ExercisePicker.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { entry ->
                val sessionId = entry.arguments?.getLong("sessionId") ?: 0L
                ExercisePickerScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onExerciseSelected = { sid, exId -> navController.navigate(Screen.LogSet.createRoute(sid, exId)) }
                )
            }

            composable(
                route = Screen.LogSet.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.LongType },
                    navArgument("exerciseId") { type = NavType.LongType }
                )
            ) { entry ->
                val sessionId = entry.arguments?.getLong("sessionId") ?: 0L
                val exerciseId = entry.arguments?.getLong("exerciseId") ?: 0L
                LogSetScreen(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    onBack = { navController.popBackStack(Screen.WorkoutSessionDetail.createRoute(sessionId), false) }
                )
            }

            composable(
                route = Screen.ExerciseHistory.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
            ) { entry ->
                val exerciseId = entry.arguments?.getLong("exerciseId") ?: 0L
                ExerciseHistoryScreen(exerciseId = exerciseId, onBack = { navController.popBackStack() })
            }
        }
    }
}
