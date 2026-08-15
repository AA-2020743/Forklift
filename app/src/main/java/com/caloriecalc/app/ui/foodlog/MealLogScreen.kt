package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.foodItemIcon
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.components.mealSlotAccentColor
import com.caloriecalc.app.ui.components.mealSlotIcon
import com.caloriecalc.app.ui.components.stableAccentColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLogScreen(
    mealSlotId: Long,
    onBack: () -> Unit,
    onAddFood: (Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: MealLogViewModel = viewModel(
        factory = SimpleViewModelFactory {
            MealLogViewModel(mealSlotId, container.nutritionLogRepository, container.mealSlotRepository)
        }
    )
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val mealSlot by viewModel.mealSlot.collectAsStateWithLifecycle()
    val mealName = mealSlot?.name ?: "Meal"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FoodIconBadge(
                            icon = mealSlotIcon(mealName),
                            accentColor = mealSlotAccentColor(mealName),
                            size = 30.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(mealName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddFood(mealSlotId) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add food")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nothing logged yet for ${mealName.lowercase()}.")
            }
        } else {
            val totalCalories = entries.sumOf { it.entry.calories }.roundToInt()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "$totalCalories kcal total",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(entries, key = { it.entry.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FoodIconBadge(
                                    icon = foodItemIcon(item.food.name),
                                    accentColor = stableAccentColor(item.food.name),
                                    size = 36.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(item.food.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = "${formatGrams(item.entry.grams)} g · ${item.entry.calories.roundToInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteEntry(item) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
