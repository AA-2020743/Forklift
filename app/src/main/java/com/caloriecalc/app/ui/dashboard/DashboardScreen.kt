package com.caloriecalc.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.data.local.entity.ActivityLog
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.domain.ActivityCalculator
import com.caloriecalc.app.domain.ActivityType
import com.caloriecalc.app.ui.components.CalorieRing
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.MacroRangeBar
import com.caloriecalc.app.ui.components.SectionHeader
import com.caloriecalc.app.ui.components.activityTypeIcon
import com.caloriecalc.app.ui.components.isMainMeal
import com.caloriecalc.app.ui.components.mealSlotAccentColor
import com.caloriecalc.app.ui.components.mealSlotIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onMealClick: (mealSlotId: Long) -> Unit,
    onMicronutrientsClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: DashboardViewModel = viewModel(
        factory = SimpleViewModelFactory {
            DashboardViewModel(
                container.profileRepository,
                container.nutritionLogRepository,
                container.mealSlotRepository,
                container.workoutRepository,
                container.activityRepository
            )
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogActivityDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Today") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onQuickAdd,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 1.dp, pressedElevation = 3.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Quickly scan or add a food")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                                    )
                                )
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CalorieRing(consumed = state.calorieConsumed, target = state.calorieTarget)
                        Text(
                            text = "${state.calorieConsumed} / ${state.calorieTarget} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.BarChart, "Macros")
                        Spacer(modifier = Modifier.height(12.dp))
                        MacroRangeBar(label = "Protein", progress = state.proteinProgress, icon = Icons.Filled.SetMeal)
                        Spacer(modifier = Modifier.height(12.dp))
                        MacroRangeBar(label = "Fat", progress = state.fatProgress, icon = Icons.Filled.WaterDrop)
                        Spacer(modifier = Modifier.height(12.dp))
                        MacroRangeBar(label = "Carbs", progress = state.carbProgress, icon = Icons.Filled.RiceBowl)
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), onClick = onMicronutrientsClick) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(Icons.Filled.Medication, "Micronutrients")
                        Text("View details", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                SectionHeader(Icons.Filled.RestaurantMenu, "Meals")
            }

            items(state.mealSummaries, key = { it.mealSlot.id }) { meal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onMealClick(meal.mealSlot.id) },
                    colors = CardDefaults.cardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FoodIconBadge(
                                icon = mealSlotIcon(meal.mealSlot.name),
                                accentColor = mealSlotAccentColor(meal.mealSlot.name)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(meal.mealSlot.name, fontWeight = FontWeight.Medium)
                                    if (meal.itemCount > 0 && isMainMeal(meal.mealSlot.name)) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        ProteinPill(meal.proteinGrams)
                                    }
                                }
                                Text(
                                    text = if (meal.itemCount == 0) "No items logged" else "${meal.itemCount} item(s)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text("${meal.calories} kcal", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                HorizontalDivider()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(Icons.Filled.DirectionsRun, "Activity")
                    if (state.totalCaloriesBurned > 0) {
                        Text(
                            "${state.totalCaloriesBurned} kcal burned",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.activityRows.isEmpty()) {
                item {
                    Text(
                        "No activity logged yet today — walks, lifting, boxing, anything.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    state.activityRows,
                    key = { row ->
                        when (row) {
                            is ActivityRow.Lifting -> "lifting_${row.session.id}"
                            is ActivityRow.Cardio -> "cardio_${row.activity.id}"
                        }
                    }
                ) { row -> ActivityRowCard(row, onDeleteCardio = viewModel::deleteActivity) }
            }

            item {
                OutlinedButton(
                    onClick = { showLogActivityDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log activity")
                }
            }
        }
    }

    if (showLogActivityDialog) {
        LogActivityDialog(
            bodyWeightKg = state.bodyWeightKg,
            onDismiss = { showLogActivityDialog = false },
            onLog = { type, duration, steps, override ->
                viewModel.logActivity(type, duration, steps, override)
                showLogActivityDialog = false
            }
        )
    }
}

/** Small rounded call-out for a main meal's protein — reuses the same protein icon as the
 * Macros card so it reads as "the same nutrient" at a glance. */
@Composable
private fun ProteinPill(grams: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.SetMeal,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "${grams}g protein",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun ActivityRowCard(row: ActivityRow, onDeleteCardio: (ActivityLog) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (row) {
                is ActivityRow.Lifting -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FoodIconBadge(icon = Icons.Filled.FitnessCenter, accentColor = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(row.session.name ?: "Workout", fontWeight = FontWeight.Medium)
                            Text(
                                text = row.durationMinutes?.let { "$it min" } ?: "In progress",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(row.caloriesBurned?.let { "$it kcal" } ?: "—", style = MaterialTheme.typography.bodyLarge)
                }
                is ActivityRow.Cardio -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FoodIconBadge(
                            icon = activityTypeIcon(row.activity.type),
                            accentColor = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(row.activity.type.displayName, fontWeight = FontWeight.Medium)
                            val stepsSuffix = row.activity.steps?.let { " · $it steps" } ?: ""
                            Text(
                                text = "${row.activity.durationMinutes} min$stepsSuffix",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${row.activity.caloriesBurned} kcal", style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { onDeleteCardio(row.activity) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogActivityDialog(
    bodyWeightKg: Double,
    onDismiss: () -> Unit,
    onLog: (ActivityType, Int, Int?, Int?) -> Unit
) {
    var selectedType by remember { mutableStateOf(ActivityType.WALKING) }
    var durationText by remember { mutableStateOf("30") }
    var stepsText by remember { mutableStateOf("") }
    var overrideText by remember { mutableStateOf("") }

    val duration = durationText.toIntOrNull() ?: 0
    val estimated = ActivityCalculator.estimateCaloriesBurned(selectedType.met, duration, bodyWeightKg)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log activity") },
        text = {
            Column {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ActivityType.entries.toList()) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = activityTypeIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (selectedType.tracksSteps) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stepsText,
                        onValueChange = { stepsText = it },
                        label = { Text("Steps (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = overrideText,
                    onValueChange = { overrideText = it },
                    label = { Text("Calories burned (estimated $estimated)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLog(selectedType, duration, stepsText.toIntOrNull(), overrideText.toIntOrNull()) },
                enabled = duration > 0
            ) { Text("Log") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
