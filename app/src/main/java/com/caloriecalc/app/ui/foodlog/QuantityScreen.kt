package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caloriecalc.app.data.repository.gramsForServings
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.components.epochMillisForMealTime
import com.caloriecalc.app.ui.components.mealSlotAccentColor
import com.caloriecalc.app.ui.components.mealSlotIcon
import com.caloriecalc.app.ui.components.mealTimeParts
import com.caloriecalc.app.ui.navigation.QUICK_ADD_MEAL_SLOT_ID
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class QuantityMode { GRAMS, SERVINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityScreen(
    foodId: Long,
    mealSlotId: Long,
    epochDay: Long,
    onBack: () -> Unit,
    onEditFood: (Long) -> Unit,
    onLogged: () -> Unit
) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()
    val isQuickAdd = mealSlotId == QUICK_ADD_MEAL_SLOT_ID

    val food by container.foodRepository.observeById(foodId)
        .collectAsStateWithLifecycle(initialValue = null)

    var mealName by remember { mutableStateOf("meal") }
    LaunchedEffect(mealSlotId) {
        if (!isQuickAdd) {
            mealName = container.mealSlotRepository.getById(mealSlotId)?.name ?: "meal"
        }
    }

    // Quick-add entries arrive with no meal chosen yet — offer a picker, defaulting to the
    // first active meal so "Log" works immediately without forcing a choice.
    val activeMealSlots by container.mealSlotRepository.observeActive().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedMealSlotId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(activeMealSlots) {
        if (isQuickAdd && selectedMealSlotId == null && activeMealSlots.isNotEmpty()) {
            selectedMealSlotId = activeMealSlots.first().id
        }
    }
    val targetMealSlotId = if (isQuickAdd) selectedMealSlotId else mealSlotId
    val targetMealName = if (isQuickAdd) activeMealSlots.find { it.id == selectedMealSlotId }?.name ?: "meal" else mealName
    var storedMealTime by remember { mutableStateOf<Long?>(null) }
    var mealTimeLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(targetMealSlotId) {
        mealTimeLoaded = false
        storedMealTime = targetMealSlotId?.let { targetId ->
            container.nutritionLogRepository.getMealTime(epochDay, targetId)
        }
        mealTimeLoaded = targetMealSlotId != null
    }

    // The meal's "eaten at" time defaults to that meal slot's configured time of day, falling
    // back to now when it has none set. It's adjustable and drives protein-spacing reminders.
    val targetMealSlot = activeMealSlots.find { it.id == targetMealSlotId }
    val nowFallback = remember { LocalTime.now() }
    val mealTime = storedMealTime?.let { mealTimeParts(it) }
        ?: targetMealSlot?.let { slot ->
            if (slot.hasTargetTime) (slot.targetHour!! to slot.targetMinute!!)
            else (nowFallback.hour to nowFallback.minute)
        }
        ?: (nowFallback.hour to nowFallback.minute)

    val currentFood = food
    if (currentFood == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Loading…") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    var mode by remember { mutableStateOf(QuantityMode.GRAMS) }
    var amountText by remember { mutableStateOf(currentFood.servingSizeGrams?.let { "1" } ?: "100") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val grams = when (mode) {
        QuantityMode.GRAMS -> amount
        QuantityMode.SERVINGS -> currentFood.gramsForServings(amount)
    }
    val factor = grams / 100.0
    val calories = currentFood.caloriesPer100g * factor
    val protein = currentFood.proteinPer100g * factor
    val fat = currentFood.fatPer100g * factor
    val carbs = currentFood.carbsPer100g * factor

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(currentFood.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { onEditFood(currentFood.id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit food details")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isQuickAdd) {
                Text("Log to", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeMealSlots, key = { it.id }) { slot ->
                        FilterChip(
                            selected = selectedMealSlotId == slot.id,
                            onClick = { selectedMealSlotId = slot.id },
                            label = { Text(slot.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = mealSlotIcon(slot.name),
                                    contentDescription = null,
                                    tint = mealSlotAccentColor(slot.name),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (currentFood.servingSizeGrams != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == QuantityMode.GRAMS,
                        onClick = { mode = QuantityMode.GRAMS; amountText = "100" },
                        label = { Text("Grams") }
                    )
                    FilterChip(
                        selected = mode == QuantityMode.SERVINGS,
                        onClick = { mode = QuantityMode.SERVINGS; amountText = "1" },
                        label = { Text("Servings (${formatGrams(currentFood.servingSizeGrams)} g${currentFood.servingName?.let { " · $it" } ?: ""})") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(if (mode == QuantityMode.GRAMS) "Grams" else "Servings") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Preview", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    NutrientRow("Calories", calories, "kcal")
                    NutrientRow("Fat", fat, "g")
                    NutrientRow("Carbs", carbs, "g")
                    NutrientRow("Protein", protein, "g")
                    CalorieMacroMismatchWarning(
                        calories = currentFood.caloriesPer100g,
                        protein = currentFood.proteinPer100g,
                        fat = currentFood.fatPer100g,
                        carbs = currentFood.carbsPer100g
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val target = targetMealSlotId ?: return@Button
                    scope.launch {
                        container.nutritionLogRepository.logFood(
                            currentFood,
                            target,
                            epochDay,
                            grams.coerceAtLeast(0.0),
                            epochMillisForMealTime(epochDay, mealTime.first, mealTime.second),
                            setMealTime = true
                        )
                        onLogged()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = grams > 0.0 && targetMealSlotId != null && mealTimeLoaded
            ) {
                Text("Log to $targetMealName")
            }
        }
    }

}

@Composable
private fun NutrientRow(label: String, value: Double, unit: String) {
    val displayValue = if (unit == "g") formatGrams(value) else value.roundToInt().toString()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("$displayValue $unit", style = MaterialTheme.typography.bodyMedium)
    }
}
