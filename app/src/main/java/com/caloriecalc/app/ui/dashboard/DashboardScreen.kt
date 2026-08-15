package com.caloriecalc.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.CalorieRing
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.MacroRangeBar
import com.caloriecalc.app.ui.components.mealSlotAccentColor
import com.caloriecalc.app.ui.components.mealSlotIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onMealClick: (mealSlotId: Long) -> Unit,
    onMicronutrientsClick: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: DashboardViewModel = viewModel(
        factory = SimpleViewModelFactory {
            DashboardViewModel(container.profileRepository, container.nutritionLogRepository, container.mealSlotRepository)
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Today") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
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
                        Text("Macros", style = MaterialTheme.typography.titleMedium)
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
                        Text("Micronutrients", fontWeight = FontWeight.Medium)
                        Text("View details", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Text("Meals", style = MaterialTheme.typography.titleMedium)
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
                                Text(meal.mealSlot.name, fontWeight = FontWeight.Medium)
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
        }
    }
}
