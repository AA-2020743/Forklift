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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.repository.gramsForServings
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.domain.MealType
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private enum class QuantityMode { GRAMS, SERVINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityScreen(
    foodId: Long,
    mealType: MealType,
    onBack: () -> Unit,
    onLogged: () -> Unit
) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    var food by remember { mutableStateOf<FoodItem?>(null) }
    LaunchedEffect(foodId) {
        food = container.foodRepository.getById(foodId)
    }

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
                title = { Text(currentFood.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
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
                        label = { Text("Servings (${currentFood.servingSizeGrams.roundToInt()} g${currentFood.servingName?.let { " · $it" } ?: ""})") }
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
                    NutrientRow("Protein", protein, "g")
                    NutrientRow("Fat", fat, "g")
                    NutrientRow("Carbs", carbs, "g")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val today = LocalDate.now().toEpochDay()
                        container.nutritionLogRepository.logFood(currentFood, mealType, today, grams.coerceAtLeast(0.0))
                        onLogged()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = grams > 0.0
            ) {
                Text("Log to ${mealType.displayName}")
            }
        }
    }
}

@Composable
private fun NutrientRow(label: String, value: Double, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("${value.roundToInt()} $unit", style = MaterialTheme.typography.bodyMedium)
    }
}
