package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.data.repository.MealEntryWithFood
import com.caloriecalc.app.data.repository.gramsForServings
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.foodItemIcon
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.components.mealSlotAccentColor
import com.caloriecalc.app.ui.components.mealSlotIcon
import com.caloriecalc.app.ui.components.stableAccentColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLogScreen(
    mealSlotId: Long,
    epochDay: Long,
    onBack: () -> Unit,
    onAddFood: (mealSlotId: Long, epochDay: Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: MealLogViewModel = viewModel(
        key = "meal_log_${mealSlotId}_$epochDay",
        factory = SimpleViewModelFactory {
            MealLogViewModel(
                mealSlotId,
                epochDay,
                container.nutritionLogRepository,
                container.mealSlotRepository,
                container.mealTemplateRepository
            )
        }
    )
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val yesterdayEntries by viewModel.yesterdayEntries.collectAsStateWithLifecycle()
    val mealSlot by viewModel.mealSlot.collectAsStateWithLifecycle()
    val mealName = mealSlot?.name ?: "Meal"
    val isToday = viewModel.isToday
    var editingEntry by remember { mutableStateOf<MealEntryWithFood?>(null) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }

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
                        Column {
                            Text(mealName)
                            if (!isToday) {
                                Text(
                                    text = LocalDate.ofEpochDay(epochDay)
                                        .format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (entries.isNotEmpty()) {
                        IconButton(onClick = { showSaveTemplateDialog = true }) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save as template")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddFood(mealSlotId, epochDay) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add food")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isToday) "Nothing logged yet for ${mealName.lowercase()}."
                    else "Nothing was logged for ${mealName.lowercase()} on this day."
                )
                if (yesterdayEntries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { viewModel.repeatPreviousDay() }) {
                        Icon(Icons.Filled.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Repeat previous day (${yesterdayEntries.size} item${if (yesterdayEntries.size == 1) "" else "s"})")
                    }
                }
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
                            Row {
                                IconButton(onClick = { editingEntry = item }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit ${item.food.name}")
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

    val currentEditingEntry = editingEntry
    if (currentEditingEntry != null) {
        EditQuantityDialog(
            entryWithFood = currentEditingEntry,
            onDismiss = { editingEntry = null },
            onSave = { grams ->
                viewModel.updateEntryQuantity(currentEditingEntry, grams)
                editingEntry = null
            }
        )
    }

    if (showSaveTemplateDialog) {
        var templateName by remember { mutableStateOf(mealName) }
        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            title = { Text("Save as template") },
            text = {
                Column {
                    Text(
                        "Saves these ${entries.size} item(s) so you can log them again in one tap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Template name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveAsTemplate(templateName.trim().ifBlank { mealName })
                        showSaveTemplateDialog = false
                    },
                    enabled = templateName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private enum class EditQuantityMode { GRAMS, SERVINGS }

@Composable
private fun EditQuantityDialog(
    entryWithFood: MealEntryWithFood,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    val food = entryWithFood.food
    var mode by remember { mutableStateOf(EditQuantityMode.GRAMS) }
    var amountText by remember { mutableStateOf(formatGrams(entryWithFood.entry.grams)) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val grams = when (mode) {
        EditQuantityMode.GRAMS -> amount
        EditQuantityMode.SERVINGS -> food.gramsForServings(amount)
    }
    val factor = grams / 100.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${food.name}") },
        text = {
            Column {
                if (food.servingSizeGrams != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = mode == EditQuantityMode.GRAMS,
                            onClick = {
                                mode = EditQuantityMode.GRAMS
                                amountText = formatGrams(grams)
                            },
                            label = { Text("Grams") }
                        )
                        FilterChip(
                            selected = mode == EditQuantityMode.SERVINGS,
                            onClick = {
                                mode = EditQuantityMode.SERVINGS
                                amountText = formatGrams(grams / (food.servingSizeGrams ?: 100.0))
                            },
                            label = { Text("Servings (${formatGrams(food.servingSizeGrams)} g)") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (mode == EditQuantityMode.GRAMS) "Grams" else "Servings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${(food.caloriesPer100g * factor).roundToInt()} kcal · " +
                        "F ${formatGrams(food.fatPer100g * factor)}g · " +
                        "C ${formatGrams(food.carbsPer100g * factor)}g · " +
                        "P ${formatGrams(food.proteinPer100g * factor)}g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(grams) }, enabled = grams > 0.0) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
