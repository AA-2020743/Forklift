package com.caloriecalc.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.dashboard.DashboardScreen
import com.caloriecalc.app.ui.dashboard.MicronutrientsScreen
import com.caloriecalc.app.ui.foodlog.AddFoodScreen
import com.caloriecalc.app.ui.foodlog.BarcodeScanScreen
import com.caloriecalc.app.ui.foodlog.EditFoodScreen
import com.caloriecalc.app.ui.foodlog.ManualFoodEntryScreen
import com.caloriecalc.app.ui.foodlog.MealLogScreen
import com.caloriecalc.app.ui.foodlog.PhotoEstimateScreen
import com.caloriecalc.app.ui.foodlog.ProteinShakeScreen
import com.caloriecalc.app.ui.foodlog.QuantityScreen
import com.caloriecalc.app.ui.foodlog.RecipeBuilderScreen
import com.caloriecalc.app.ui.profile.ProfileScreen
import com.caloriecalc.app.ui.trends.TrendsScreen
import com.caloriecalc.app.ui.weight.WeightScreen
import com.caloriecalc.app.ui.workout.ExerciseHistoryScreen
import com.caloriecalc.app.ui.workout.ExercisePickerScreen
import com.caloriecalc.app.ui.workout.LogSetScreen
import com.caloriecalc.app.ui.workout.TemplateEditorScreen
import com.caloriecalc.app.ui.workout.TemplatesScreen
import com.caloriecalc.app.ui.workout.WorkoutListScreen
import com.caloriecalc.app.ui.workout.WorkoutSessionScreen
import java.time.LocalDate
import kotlinx.coroutines.launch

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
                DashboardScreen(
                    onMealClick = { mealSlotId, epochDay ->
                        navController.navigate(Screen.MealLog.createRoute(mealSlotId, epochDay))
                    },
                    onMicronutrientsClick = { navController.navigate(Screen.Micronutrients.route) },
                    onQuickAdd = { navController.navigate(Screen.AddFood.createRoute(QUICK_ADD_MEAL_SLOT_ID)) },
                    onOpenTrends = { navController.navigate(Screen.WeeklySummary.route) }
                )
            }

            composable(Screen.WeeklySummary.route) {
                TrendsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Micronutrients.route) {
                MicronutrientsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Workouts.route) {
                WorkoutListScreen(
                    onSessionClick = { id -> navController.navigate(Screen.WorkoutSessionDetail.createRoute(id)) },
                    onManageTemplates = { navController.navigate(Screen.Templates.route) }
                )
            }

            composable(Screen.Templates.route) {
                TemplatesScreen(
                    onBack = { navController.popBackStack() },
                    onTemplateClick = { navController.navigate(Screen.TemplateEditor.createRoute(it)) }
                )
            }

            composable(
                route = Screen.TemplateEditor.route,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { entry ->
                val templateId = entry.arguments?.getLong("templateId") ?: 0L
                TemplateEditorScreen(
                    templateId = templateId,
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate(Screen.TemplateExercisePicker.createRoute(it)) }
                )
            }

            composable(
                route = Screen.TemplateExercisePicker.route,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { entry ->
                val templateId = entry.arguments?.getLong("templateId") ?: 0L
                val container = rememberAppContainer()
                val scope = rememberCoroutineScope()
                ExercisePickerScreen(
                    onBack = { navController.popBackStack() },
                    onExerciseSelected = { exerciseId ->
                        scope.launch {
                            container.workoutTemplateRepository.addExercise(templateId, exerciseId)
                            navController.popBackStack()
                        }
                    }
                )
            }
            composable(Screen.Weight.route) { WeightScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }

            composable(
                route = Screen.MealLog.route,
                arguments = listOf(
                    navArgument("mealSlotId") { type = NavType.LongType },
                    navArgument("epochDay") { type = NavType.LongType }
                )
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                val epochDay = entry.arguments?.getLong("epochDay") ?: LocalDate.now().toEpochDay()
                MealLogScreen(
                    mealSlotId = mealSlotId,
                    epochDay = epochDay,
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
                    onPhotoEstimate = { navController.navigate(Screen.PhotoEstimate.createRoute(it)) },
                    onFoodSelected = { foodId, mid -> navController.navigate(Screen.Quantity.createRoute(foodId, mid)) },
                    onQuickAdded = { mid ->
                        navController.popBackStack(
                            Screen.MealLog.createRoute(mid, LocalDate.now().toEpochDay()),
                            false
                        )
                    },
                    onEditFood = { foodId -> navController.navigate(Screen.EditFood.createRoute(foodId)) },
                    onCreateProteinShake = { navController.navigate(Screen.ProteinShake.createRoute(it)) },
                    onCreateRecipe = { navController.navigate(Screen.RecipeBuilder.createRoute(it)) }
                )
            }

            composable(
                route = Screen.RecipeBuilder.route,
                arguments = listOf(navArgument("mealSlotId") { type = NavType.LongType })
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                RecipeBuilderScreen(
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onSaved = { foodId, mid -> navController.navigate(Screen.Quantity.createRoute(foodId, mid)) }
                )
            }

            composable(
                route = Screen.EditFood.route,
                arguments = listOf(navArgument("foodId") { type = NavType.LongType })
            ) { entry ->
                val foodId = entry.arguments?.getLong("foodId") ?: 0L
                EditFoodScreen(
                    foodId = foodId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ProteinShake.route,
                arguments = listOf(navArgument("mealSlotId") { type = NavType.LongType })
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                ProteinShakeScreen(
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onSaved = { foodId, mid -> navController.navigate(Screen.Quantity.createRoute(foodId, mid)) }
                )
            }

            composable(
                route = Screen.PhotoEstimate.route,
                arguments = listOf(navArgument("mealSlotId") { type = NavType.LongType })
            ) { entry ->
                val mealSlotId = entry.arguments?.getLong("mealSlotId") ?: 0L
                PhotoEstimateScreen(
                    mealSlotId = mealSlotId,
                    onBack = { navController.popBackStack() },
                    onLogged = {
                        navController.popBackStack(
                            Screen.MealLog.createRoute(mealSlotId, LocalDate.now().toEpochDay()),
                            false
                        )
                    }
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
                    onLogged = {
                        if (mealSlotId == QUICK_ADD_MEAL_SLOT_ID) {
                            navController.popBackStack(Screen.Dashboard.route, false)
                        } else {
                            navController.popBackStack(
                                Screen.MealLog.createRoute(mealSlotId, LocalDate.now().toEpochDay()),
                                false
                            )
                        }
                    }
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
                    onBack = { navController.popBackStack() },
                    onExerciseSelected = { exerciseId -> navController.navigate(Screen.LogSet.createRoute(sessionId, exerciseId)) }
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
