package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.Micronutrients
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.foodItemIcon
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.components.stableAccentColor
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private enum class ShakeLiquid { WATER, MILK }

/**
 * Builds a two-ingredient combo food: a fixed powder serving (entered directly, the way a
 * scoop is measured by the tub's label, not by weighing it) plus an optional liquid whose
 * contribution scales with real grams — water at zero, milk from an existing food at
 * `grams/100 * milk's per-100g values`. The result is stored as FoodRepository.createManualFood
 * would store any other food: per-100g values with servingSizeGrams = 100 and servingName =
 * "1 shake", so logging "1 serving" later reproduces this exact total.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProteinShakeScreen(
    mealSlotId: Long,
    onBack: () -> Unit,
    onSaved: (foodId: Long, mealSlotId: Long) -> Unit
) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("Protein Shake") }
    var powderCalories by remember { mutableStateOf("") }
    var powderFat by remember { mutableStateOf("") }
    var powderCarbs by remember { mutableStateOf("") }
    var powderProtein by remember { mutableStateOf("") }

    var liquid by remember { mutableStateOf(ShakeLiquid.WATER) }

    var milkQuery by remember { mutableStateOf("milk") }
    var milkResults by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var selectedMilk by remember { mutableStateOf<FoodItem?>(null) }
    var milkGramsText by remember { mutableStateOf("") }

    var showCreateMilkForm by remember { mutableStateOf(false) }
    var newMilkName by remember { mutableStateOf("Milk") }
    var newMilkCalories by remember { mutableStateOf("") }
    var newMilkFat by remember { mutableStateOf("") }
    var newMilkCarbs by remember { mutableStateOf("") }
    var newMilkProtein by remember { mutableStateOf("") }

    LaunchedEffect(milkQuery, liquid) {
        milkResults = if (liquid == ShakeLiquid.MILK && milkQuery.isNotBlank()) {
            container.foodRepository.searchLocal(milkQuery).first()
        } else {
            emptyList()
        }
    }

    val powderCal = powderCalories.toDoubleOrNull() ?: 0.0
    val powderF = powderFat.toDoubleOrNull() ?: 0.0
    val powderC = powderCarbs.toDoubleOrNull() ?: 0.0
    val powderP = powderProtein.toDoubleOrNull() ?: 0.0

    val milkGrams = milkGramsText.toDoubleOrNull() ?: 0.0
    val milkFactor = milkGrams / 100.0
    val milk = selectedMilk
    val milkCal = if (liquid == ShakeLiquid.MILK && milk != null) milk.caloriesPer100g * milkFactor else 0.0
    val milkF = if (liquid == ShakeLiquid.MILK && milk != null) milk.fatPer100g * milkFactor else 0.0
    val milkC = if (liquid == ShakeLiquid.MILK && milk != null) milk.carbsPer100g * milkFactor else 0.0
    val milkP = if (liquid == ShakeLiquid.MILK && milk != null) milk.proteinPer100g * milkFactor else 0.0
    val milkMicros = if (liquid == ShakeLiquid.MILK && milk != null) milk.micronutrients.scaledBy(milkFactor) else Micronutrients()

    val totalCalories = powderCal + milkCal
    val totalFat = powderF + milkF
    val totalCarbs = powderC + milkC
    val totalProtein = powderP + milkP

    val powderValuesValid = listOf(powderProtein, powderFat, powderCarbs).all(::isNonNegativeNumberOrBlank)
    val isValid = name.isNotBlank() &&
        powderCalories.toDoubleOrNull()?.let { it.isFinite() && it >= 0.0 } == true &&
        powderValuesValid &&
        (liquid == ShakeLiquid.WATER || (milk != null && milkGrams > 0))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create protein shake") },
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
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text("Powder (per serving/scoop)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Same numbers as the tub's label for one scoop — not scaled by weight.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = powderCalories,
                onValueChange = { powderCalories = it },
                label = { Text("Calories") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = powderFat,
                onValueChange = { powderFat = it },
                label = { Text("Fat g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = powderCarbs,
                onValueChange = { powderCarbs = it },
                label = { Text("Carbs g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = powderProtein,
                onValueChange = { powderProtein = it },
                label = { Text("Protein g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Mixed with", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = liquid == ShakeLiquid.WATER,
                    onClick = { liquid = ShakeLiquid.WATER },
                    label = { Text("Water") }
                )
                FilterChip(
                    selected = liquid == ShakeLiquid.MILK,
                    onClick = { liquid = ShakeLiquid.MILK },
                    label = { Text("Milk") }
                )
            }

            if (liquid == ShakeLiquid.MILK) {
                Spacer(modifier = Modifier.height(12.dp))
                val currentMilk = milk
                if (currentMilk == null) {
                    OutlinedTextField(
                        value = milkQuery,
                        onValueChange = { milkQuery = it },
                        label = { Text("Search your foods") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    milkResults.take(5).forEach { food ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            onClick = { selectedMilk = food }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FoodIconBadge(
                                    icon = foodItemIcon(food.name),
                                    accentColor = stableAccentColor(food.name),
                                    size = 28.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = food.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${food.caloriesPer100g.roundToInt()} kcal / 100g",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!showCreateMilkForm) {
                        TextButton(onClick = { showCreateMilkForm = true }) {
                            Text("Can't find it? Create milk")
                        }
                    } else {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("New food (per 100g)", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newMilkName,
                                    onValueChange = { newMilkName = it },
                                    label = { Text("Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newMilkCalories,
                                    onValueChange = { newMilkCalories = it },
                                    label = { Text("Calories / 100g") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newMilkFat,
                                    onValueChange = { newMilkFat = it },
                                    label = { Text("Fat g / 100g") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newMilkCarbs,
                                    onValueChange = { newMilkCarbs = it },
                                    label = { Text("Carbs g / 100g") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newMilkProtein,
                                    onValueChange = { newMilkProtein = it },
                                    label = { Text("Protein g / 100g") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val created = container.foodRepository.createManualFood(
                                                name = newMilkName.trim().ifBlank { "Milk" },
                                                brand = null,
                                                caloriesPer100g = newMilkCalories.toDoubleOrNull() ?: 0.0,
                                                proteinPer100g = newMilkProtein.toDoubleOrNull() ?: 0.0,
                                                fatPer100g = newMilkFat.toDoubleOrNull() ?: 0.0,
                                                carbsPer100g = newMilkCarbs.toDoubleOrNull() ?: 0.0,
                                                servingSizeGrams = null,
                                                servingName = null
                                            )
                                            selectedMilk = created
                                            showCreateMilkForm = false
                                        }
                                    },
                                     enabled = newMilkCalories.toDoubleOrNull()
                                         ?.let { it.isFinite() && it >= 0.0 } == true &&
                                         listOf(newMilkProtein, newMilkFat, newMilkCarbs)
                                             .all(::isNonNegativeNumberOrBlank),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Create & select") }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FoodIconBadge(
                                icon = Icons.Filled.LocalDrink,
                                accentColor = stableAccentColor(currentMilk.name),
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(currentMilk.name, style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(onClick = { selectedMilk = null }) { Text("Change") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = milkGramsText,
                        onValueChange = { milkGramsText = it },
                        label = { Text("Grams of milk used") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("This shake", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${totalCalories.roundToInt()} kcal")
                    Text("Fat: ${formatGrams(totalFat)} g")
                    Text("Carbs: ${formatGrams(totalCarbs)} g")
                    Text("Protein: ${formatGrams(totalProtein)} g")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    scope.launch {
                        val food = container.foodRepository.createManualFood(
                            name = name.trim(),
                            brand = null,
                            caloriesPer100g = totalCalories,
                            proteinPer100g = totalProtein,
                            fatPer100g = totalFat,
                            carbsPer100g = totalCarbs,
                            servingSizeGrams = 100.0,
                            servingName = "1 shake",
                            micronutrients = milkMicros
                        )
                        onSaved(food.id, mealSlotId)
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save & continue")
            }
        }
    }
}
